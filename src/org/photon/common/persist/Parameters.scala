package org.photon.common.persist

import java.sql.{ResultSet, PreparedStatement}

object Parameters {
  implicit object ByteParameter extends Parameter[Byte] {
    def get(rs: ResultSet, index: Int) = rs.getByte(index)
    def get(rs: ResultSet, name: String) = rs.getByte(name)

    def set(ps: PreparedStatement, index: Int, value: Byte) = ps.setByte(index, value)
  }

  implicit object ShortParameter extends Parameter[Short] {
    def get(rs: ResultSet, index: Int) = rs.getShort(index)
    def get(rs: ResultSet, name: String) = rs.getShort(name)

    def set(ps: PreparedStatement, index: Int, value: Short) = ps.setShort(index, value)
  }

  implicit object IntParameter extends Parameter[Int] {
    def get(rs: ResultSet, index: Int) = rs.getInt(index)
    def get(rs: ResultSet, name: String) = rs.getInt(name)

    def set(ps: PreparedStatement, index: Int, value: Int) = ps.setInt(index, value)
  }

  implicit object LongParameter extends Parameter[Long] {
    def get(rs: ResultSet, index: Int) = rs.getLong(index)
    def get(rs: ResultSet, name: String) = rs.getLong(name)

    def set(ps: PreparedStatement, index: Int, value: Long) = ps.setLong(index, value)
  }

  implicit object StringParameter extends Parameter[String] {
    def get(rs: ResultSet, index: Int) = rs.getString(index)
    def get(rs: ResultSet, name: String) = rs.getString(name)

    def set(ps: PreparedStatement, index: Int, value: String) = ps.setString(index, value)
  }

}
