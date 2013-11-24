package org.photon.common.persist

import com.twitter.util.Future

trait Cachable[T <: Model] extends Repository[T] {
  def hydrate(): Future[Unit]

  def find(predicate: T => Boolean): Option[T]
  def filter(predicate: T => Boolean): Iterable[T]
}
