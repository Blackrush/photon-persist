package org.photon.common.persist

import scala.collection.generic.CanBuildFrom
import scala.collection.{TraversableLike, mutable}

object Utils {
  class OptionBuilder[T](var option: Option[T]) extends mutable.Builder[T, Option[T]] {
    def +=(elem: T) = {
      if (option.isDefined) throw new IllegalStateException("an Option can store one, and only one, instance")
      option = Some(elem)
      this
    }

    def clear() = option = None

    def result() = option
  }

  implicit def optionCanBuildFrom[T] = new CanBuildFrom[TraversableLike[T, _], T, Option[T]] {
    def apply(from: TraversableLike[T, _]) = new OptionBuilder(from.headOption)
    def apply() = new OptionBuilder(None)
  }
}
