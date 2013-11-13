import java.sql.{ResultSet, PreparedStatement}
import java.util.concurrent.Executors
import org.photon.common.persist.{BaseRepository, ModelState, Model}

object Example {
  import ModelState.ModelState
  import org.photon.common.persist.Parameters._

  implicit val e = Executors.newCachedThreadPool()

  case class MyModel(id: Long, name: String, someValue: Int, state: ModelState = ModelState.None) extends Model {
    type PrimaryKey = Long
  }

  object MyRepository extends BaseRepository[MyModel](null) {
    import org.photon.common.persist.Connections._

    lazy val table = "my_model"
    lazy val pkColumns = Seq("id")
    lazy val columns = Seq("name", "some_value")

    def buildModel(rs: ResultSet) = MyModel(
      rs.getLong("id"),
      rs.getString("name"),
      rs.getInt("some_value")
    )

    def bindParams(ps: PreparedStatement, o: MyModel) {
      ps.set(1, o.name)
      ps.set(2, o.someValue)
    }

    def setPersisted(o: MyModel, newId: MyModel#PrimaryKey) = o.copy(id = newId, state = ModelState.Persisted)
    def setRemoved(o: MyModel) = o.copy(state = ModelState.Removed)
  }

  def main(args: Array[String]) {
    val repo = MyRepository

    println(repo.selectQuery)
    println(repo.insertQuery)
    println(repo.updateQuery)
    println(repo.deleteQuery)
  }
}
