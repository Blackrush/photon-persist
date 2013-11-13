package org.photon.common.persist

import java.sql.{Timestamp, ResultSet, PreparedStatement}
import com.twitter.util.{Duration, Time}
import scala.concurrent.duration.{TimeUnit, MILLISECONDS}

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

  implicit object TimeParameter extends Parameter[Time] {
    def get(rs: ResultSet, index: Int) = Time(rs.getTimestamp(index))
    def get(rs: ResultSet, name: String) = Time(rs.getTimestamp(name))

    def set(ps: PreparedStatement, index: Int, value: Time) = ps.setTimestamp(index, new Timestamp(value.inMilliseconds))
  }

  implicit object FloatParameter extends Parameter[Float] {
    def get(rs: ResultSet, index: Int) = rs.getFloat(index)
    def get(rs: ResultSet, name: String) = rs.getFloat(name)

    def set(ps: PreparedStatement, index: Int, value: Float) = ps.setFloat(index, value)
  }

  implicit object DoubleParameter extends Parameter[Double] {
    def get(rs: ResultSet, index: Int) = rs.getDouble(index)
    def get(rs: ResultSet, name: String) = rs.getDouble(name)

    def set(ps: PreparedStatement, index: Int, value: Double) = ps.setDouble(index, value)
  }

  implicit object BooleanParameter extends Parameter[Boolean] {
    def get(rs: ResultSet, index: Int) = rs.getBoolean(index)
    def get(rs: ResultSet, name: String) = rs.getBoolean(name)

    def set(ps: PreparedStatement, index: Int, value: Boolean) = ps.setBoolean(index, value)
  }

  implicit def OptionParameter[T](implicit p: Parameter[T], ev: Null <:< T) = new Parameter[Option[T]] {
    def get(rs: ResultSet, index: Int) = Option(p.get(rs, index))
    def get(rs: ResultSet, name: String) = Option(p.get(rs, name))

    def set(ps: PreparedStatement, index: Int, value: Option[T]) = p.set(ps, index, value.orNull)
  }

  implicit def DurationParameter(implicit timeUnit: TimeUnit = MILLISECONDS) = new Parameter[Duration] {
    def get(rs: ResultSet, index: Int) = Duration(rs.getLong(index), timeUnit)
    def get(rs: ResultSet, name: String) = Duration(rs.getLong(name), timeUnit)

    def set(ps: PreparedStatement, index: Int, value: Duration) = ps.setLong(index, value.inUnit(timeUnit))
  }

}
