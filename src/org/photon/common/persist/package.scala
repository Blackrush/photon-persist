package org.photon.common

package object persist {
  type Index = Incremented[Int]

  object Index {
    def apply: Index = Incremented[Int]
    def apply(value: Int): Index = Incremented(value)
  }

  implicit def int2index(i: Int): Index = Incremented(i)
}
