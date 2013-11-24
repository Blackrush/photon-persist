package org.photon.common.persist

import com.twitter.util.Future
import scala.collection.mutable

trait Caching[T <: Model] extends Repository[T] {
  val cache = mutable.Map.empty[T#PrimaryKey, T]
  private var allCached: Boolean = false

  abstract override def find(id: T#PrimaryKey): Future[T] =
    cache.get(id) match {
      case Some(o) => Future(o)
      case None => super.find(id) onSuccess { o =>
        cache(id) = o
      }
    }

  abstract override def all: Future[Seq[T]] =
    if (allCached)
      Future(cache.values.toSeq)
    else
      super.all onSuccess { res =>
        for (o <- res) cache(o.id) = o
        allCached = true
      }
}
