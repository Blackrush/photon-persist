package org.photon.common.persist

object Models {
  implicit class RichModel[T <: Model](val o: T) extends AnyVal {
    def persist(implicit repo: Repository[T]) = repo.persist(o)
    def remove(implicit repo: Repository[T]) = repo.remove(o)
  }
}
