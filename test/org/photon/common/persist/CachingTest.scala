package org.photon.common.persist

import org.scalatest.{BeforeAndAfter, Matchers, FreeSpec}
import java.sql.{DriverManager, PreparedStatement, ResultSet, Connection}
import java.util.concurrent.{Executors, Executor}
import com.twitter.util.Await

class CachingTest extends FreeSpec with Matchers with BeforeAndAfter {

  import Parameters._
  import Connections._
  import ModelState.ModelState

  case class MyModel(id: Int, value: String, state: ModelState = ModelState.None) extends Model {
    type PrimaryKey = Int
  }

  class MyRepository(connection: Connection)(implicit e: Executor)
    extends BaseRepository[MyModel](connection)
    with Caching[MyModel]
  {
    lazy val table = "my_repository"
    lazy val pkColumns = Seq("id")
    lazy val columns = Seq("value")

    def buildModel(rs: ResultSet) = MyModel(
      rs.getInt("id"),
      rs.getString("value"),
      state = ModelState.Persisted
    )

    def bindParams(ps: PreparedStatement, o: MyModel)(implicit index: Index) {
      ps.set(o.value)
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
    stmt addBatch "INSERT INTO my_repository(value) VALUES('such fake value');"
    stmt addBatch "INSERT INTO my_repository(value) VALUES('fakelol');"

    stmt.executeBatch()
  }

  after {
    val stmt = connection.createStatement()
    stmt.execute("DROP TABLE my_repository;")
  }

  "A cached repository" - {
    "should update his backing map" - {
      "when hydrated" in new Fixture {
        result(repo hydrate())

        repo.cache should not be ('empty)
      }

      "when retrieving data by id" in new Fixture {
        val v = result(repo find 1)

        repo.cache should have size 1
        repo.cache(v.id) should === (v)
      }

      "when persisting data" in new Fixture {
        val v = result(repo persist MyModel(-1, "wow"))

        repo.cache should have size 1
        repo.cache(v.id) should === (v)
      }

      "when removing data" in new Fixture {
        var v = result(repo find 1)

        repo.cache should have size 1

        v = result(repo remove v)

        repo.cache should be ('empty)
      }
    }
    
    "should not update his backing map" - {
      "when created" in new Fixture {
        repo.cache should be ('empty)
      }

      "when already containing data" in new Fixture {
        result(repo hydrate())
        
        repo.cache should not be ('empty)
        val cacheSize = repo.cache.size
        
        result(repo find 1)
        repo.cache should have size (cacheSize)
      }

      "when persisting already persisted data" in new Fixture {
        var v = result(repo find 2)

        repo.cache should have size 1

        v = result(repo persist v.copy(value = "such edit"))

        repo.cache should have size 1
      }
    }

    "allows to query cache" - {
      "through find" in new Fixture {
        result(repo hydrate())
        repo.cache should not be ('empty)

        repo.find(x => x.value == "fakelol") should not be None
      }

      "through filter" in new Fixture {
        result(repo hydrate())
        repo.cache should not be ('empty)

        repo.filter(x => x.value == "fakelol") should have size 1
      }
    }
  }
}
