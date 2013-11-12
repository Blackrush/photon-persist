package org.photon.common.persist

import java.sql.{ResultSet, Statement, PreparedStatement, Connection}
import scala.collection.generic.CanBuildFrom

object Connections {
  implicit class RichConnection[T <: Connection](val c: Connection) extends AnyVal {
    def prepare[R](query: String)(fn: PreparedStatement => R): R = {
      val stmt = c.prepareStatement(query)
      try {
        fn(stmt)
      } finally {
        stmt.close()
      }
    }
  }

  implicit class RichStatement[T <: Statement](val s: Statement) extends AnyVal {
    def result[R, Result](fn: ResultSet => R)(implicit cbf: CanBuildFrom[_, R, Result]): Result = {
      val rs = s.getResultSet
      try {
        val builder = cbf()
        while (rs.next()) builder += fn(rs)
        builder.result
      } finally {
        rs.close()
      }
    }
  }

  implicit class RichPreparedStatement[T <: PreparedStatement](val s: PreparedStatement) extends AnyVal {
    def set[R: Parameter](index: Int, value: R): Unit = implicitly[Parameter[R]].set(s, index, value)
  }

  implicit class RichResultSet[T <: ResultSet](val rs: T) extends AnyVal {
    def get[R: Parameter](index: Int): R = implicitly[Parameter[R]].get(rs, index)
    def get[R: Parameter](name: String): R = implicitly[Parameter[R]].get(rs, name)
  }
}
