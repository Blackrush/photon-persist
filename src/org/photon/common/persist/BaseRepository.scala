package org.photon.common.persist

import java.util.concurrent.Executor
import com.twitter.util.Future
import java.sql.{ResultSet, Connection, PreparedStatement}
import scala.collection.generic.CanBuildFrom

abstract class BaseRepository[T <: Model](connection: Connection)(implicit pkParam: Parameter[T#PrimaryKey], e: Executor)
  extends Repository[T]
{
  import Connections._

  val table: String
  val pkColumns: Seq[String]
  val columns: Seq[String]
  val allColumns: Seq[String] = pkColumns ++ columns

  def ids(c: Seq[String], delim: String = ", ") = c.mkString(delim) // IDENTS
  def phr(c: Seq[String], delim: String = ", ") = c.map(_ => "?").mkString(delim) // PLACEHOLDERS
  def idsWphr(c: Seq[String], delim: String = ", ") = c.map(c => c + "=?").mkString(delim) // IDENTS WITH PLACEHOLDER

  val selectQuery = s"SELECT ${ids(allColumns)} FROM $table"
  val insertQuery = s"INSERT INTO $table(${ids(columns)}) VALUES(${phr(columns)}) RETURNING (${ids(pkColumns)})"
  val updateQuery = s"UPDATE $table SET ${idsWphr(columns)} WHERE ${idsWphr(pkColumns, " AND ")}"
  val deleteQuery = s"DELETE FROM $table WHERE ${idsWphr(pkColumns, " AND ")}"

  def buildModel(rs: ResultSet): T
  def bindParams(ps: PreparedStatement, o: T)
  def setPersisted(o: T, newId: T#PrimaryKey): T
  def setRemoved(o: T): T

  def where[Result, Ignored](query: String)(fn: (PreparedStatement) => Ignored)(implicit cbf: CanBuildFrom[_, T, Result]): Future[Result] = Async {
    connection.prepare(selectQuery + " " + query) { ps =>
      fn(ps)
      ps.result(buildModel)
    }
  }

  def findById(id: T#PrimaryKey): Future[T] = where(idsWphr(pkColumns))(_.set(1, id)).map(_.head) // force single value?

  def persist(o: T): Future[T] = o.state match {
    case ModelState.None => Async {
      connection.prepare(insertQuery) { ps =>
        bindParams(ps, o)
        ps.set(columns.size, o.id: T#PrimaryKey)

        import Utils.optionCanBuildFrom
        ps.result[T#PrimaryKey, Option[T#PrimaryKey]](_.get(1)) match {
          case Some(id) => setPersisted(o, id)
          case None => throw PersistException(reason = "database did not returned new id")
        }
      }
    }

    case ModelState.Persisted => Async {
      connection.prepare(updateQuery) { ps =>
        bindParams(ps, o)
        o
      }
    }

    case ModelState.Removed => Future.exception(PersistException(reason = "you must not persist a removed model"))
  }


  def remove(o: T): Future[T] = o.state match {
    case ModelState.None | ModelState.Removed => Future(o) // nothing to do or already removed so who cares

    case ModelState.Persisted => Async {
      connection.prepare(deleteQuery) { ps =>
        ps.set(1, o.id: T#PrimaryKey)

        ps.executeUpdate() match {
          case 1 => setRemoved(o)
          case n if n <= 0 => throw PersistException("the sql delete query has not affected any rows")
          case n if n >= 2 => throw PersistException("the sql delete query has affected more than one row")
        }
      }
    }
  }
}
