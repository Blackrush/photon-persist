package org.photon.common.persist

import java.sql.{PreparedStatement, ResultSet}

trait Parameter[T] {
  def get(rs: ResultSet, index: Int): T
  def get(rs: ResultSet, name: String): T

  def set(ps: PreparedStatement, index: Int, value: T)
}
