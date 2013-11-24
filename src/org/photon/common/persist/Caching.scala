package org.photon.common.persist

import com.twitter.util.Future
import scala.collection.mutable

trait Caching[T <: Model] extends Cachable[T] {
  val cache = mutable.Map.empty[T#PrimaryKey, T]

  def hydrate(): Future[Unit] = all map { res =>
    for (o <- res) cache(o.id) = o
  }

  def find(predicate: T => Boolean): Option[T] = cache.values.find(predicate)
  def filter(predicate: T => Boolean): Iterable[T] = cache.values.filter(predicate)

  abstract override def find(id: T#PrimaryKey): Future[T] =
    cache.get(id) match {
      case Some(o) => Future(o)
      case None => super.find(id) map { o =>
        cache(id) = o
        o
      }
    }

  abstract override def persist(o: T): Future[T] = {
    val fut = super.persist(o)

    if (o.state == ModelState.None)
      fut map { o =>
        cache(o.id) = o
        o
      }
    else
      fut
  }

  abstract override def remove(o: T): Future[T] = super.remove(o) map { o =>
    cache -= o.id
    o
  }
}
