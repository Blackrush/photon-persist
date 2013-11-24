package org.photon.common.persist

import java.sql.{ResultSet, Statement, PreparedStatement, Connection}
import scala.collection.generic.CanBuildFrom

object Connections {
  implicit class RichConnection[T <: Connection](val c: Connection) extends AnyVal {
    def prepare[R](query: String, returnGeneratedKeys: Boolean = false)(fn: PreparedStatement => R): R = {
      val stmt =
        if (returnGeneratedKeys)
          c.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        else
          c.prepareStatement(query)

      try {
        fn(stmt)
      } finally {
        stmt.close()
      }
    }

    def statement[R](query: String, returnGeneratedKeys: Boolean = false)(fn: Statement => R): R = {
      val stmt = c.createStatement()

      if (returnGeneratedKeys) {
        stmt.execute(query, Statement.RETURN_GENERATED_KEYS)
      } else {
        stmt.execute(query)
      }

      try {
        fn(stmt)
      } finally {
        stmt.close()
      }
    }
  }

  implicit class RichPreparedStatement(val s: PreparedStatement) extends AnyVal {
    def set[R: Parameter](index: Int, value: R): Unit = implicitly[Parameter[R]].set(s, index, value)

    def set[R](value: R)(implicit index: Incremented[Int], p: Parameter[R]): Unit = set(index.get, value)
  }

  implicit class RichResultSet[T <: ResultSet](val rs: T) extends AnyVal {
    def get[R: Parameter](index: Int): R = implicitly[Parameter[R]].get(rs, index)
    def get[R: Parameter](name: String): R = implicitly[Parameter[R]].get(rs, name)

    def map[R, Result](fn: ResultSet => R)(implicit cbf: CanBuildFrom[_, R, Result]): Result = {
      val builder = cbf()
      while (rs.next()) builder += fn(rs)
      builder.result
    }
  }
}
