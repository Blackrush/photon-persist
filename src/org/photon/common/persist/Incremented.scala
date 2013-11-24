package org.photon.common.persist

class Incremented[@specialized(Byte, Short, Int, Long, Float, Double) T]
  (private var value: T)
  (implicit n: Numeric[T])
{
  def get: T = {
    val tmp = value
    value = n.plus(value, n.one)
    tmp
  }

  def apply(): T = get
}

object Incremented {
  def apply[T](implicit n: Numeric[T]) = new Incremented[T](n.zero)
  def apply[T](value: T)(implicit n: Numeric[T]) = new Incremented[T](value)
}