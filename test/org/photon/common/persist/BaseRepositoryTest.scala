package org.photon.common.persist

import org.scalatest.{BeforeAndAfter, ShouldMatchers, FreeSpec}
import java.sql.{DriverManager, ResultSet, PreparedStatement, Connection}
import java.util.concurrent.{Executors, Executor}
import com.twitter.util.Await

class BaseRepositoryTest extends FreeSpec with ShouldMatchers with BeforeAndAfter {

  import Parameters._
  import Connections._
  import ModelState.ModelState

  case class MyModel(id: Int, value: String, state: ModelState = ModelState.None) extends Model {
    type PrimaryKey = Int
  }

  class MyRepository(connection: Connection)(implicit e: Executor)
    extends BaseRepository[MyModel](connection)
  {
    lazy val table = "my_repository"
    lazy val pkColumns = Seq("id")
    lazy val columns = Seq("value")

    def buildModel(rs: ResultSet) = MyModel(
      rs.getInt("id"),
      rs.getString("value"),
      state = ModelState.Persisted
    )

    def bindParams(ps: PreparedStatement, o: MyModel) {
      ps.set(1, o.value)
    }

    def setPersisted(o: MyModel, newId: Int) = o.copy(id = newId, state = ModelState.Persisted)

    def setRemoved(o: MyModel) = o.copy(state = ModelState.Removed)
  }

  Class.forName(sys.props("photon-persist.tests.db.driver"))
  val connection = DriverManager.getConnection(sys.props("photon-persist.tests.db.dsn"))
  connection.setAutoCommit(false)

  trait Fixture {
    implicit val executor = Executors.newSingleThreadExecutor()
    val repo = new MyRepository(connection)
  }

  import Await.result

  before {
    val stmt = connection.createStatement()
    stmt addBatch "CREATE TABLE my_repository(" +
                  "    id serial PRIMARY KEY," +
                  "    value character varying(255) NOT NULL" +
                  ");"

    stmt addBatch "INSERT INTO my_repository(value) VALUES('such name');"

    stmt.executeBatch()
  }

  after {
    val stmt = connection.createStatement()
    stmt.execute("DROP TABLE my_repository;")
  }

  "A repository" - {

    "should well construct SQL queries" - {
      "insert" in new Fixture {
        repo.insertQuery should be === "INSERT INTO my_repository(value) VALUES(?) RETURNING (id)"
      }
      "update" in new Fixture {
        repo.updateQuery should be === "UPDATE my_repository SET value=? WHERE id=?"
      }
      "delete" in new Fixture {
        repo.deleteQuery should be === "DELETE FROM my_repository WHERE id=?"
      }
      "select" in new Fixture {
        repo.selectQuery should be === "SELECT id, value FROM my_repository"
      }
    }

    "should successfully find values" - {
      "by id" in new Fixture {
        val v = result(repo findById 1)
        v.id should be === 1
        v.value should be === "such name"
        v.state should be === ModelState.Persisted
      }

      "by value" in new Fixture {
        val v = result(repo.find("value", "such name"))
        v.id should be === 1
        v.value should be === "such name"
        v.state should be === ModelState.Persisted
      }
    }

    "should insert values" in new Fixture {
      var v = MyModel(-1, "wow")
      v.state should be === ModelState.None

      v = result(repo persist v)
      v.id should not be (-1)
      v.state should be === ModelState.Persisted
    }
    
    "should update values" in new Fixture {
      var v = result(repo findById 1)
      v.value should be === "such name"
      v.state should be === ModelState.Persisted

      v = result(repo persist v.copy(value = "such edit"))
      v.value should be === "such edit"
      v.state should be === ModelState.Persisted
    }

    "should remove values" in new Fixture {
      var v = result(repo findById 1)
      v.state should be === ModelState.Persisted

      v = result(repo remove v)
      v.state should be === ModelState.Removed
    }

  }

}
