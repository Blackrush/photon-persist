package org.photon.common.persist

import org.scalatest.{Matchers, FreeSpec}

class IncrementedTest extends FreeSpec with Matchers {
  trait Fixture {
    val i = Incremented[Int]
  }

  "An `Incremented' value" - {
    "should return default value the first time" in new Fixture {
      i.get should === (0)
    }

    "should return incremented value otherwise" in new Fixture {
      i.get should === (0)
      i.get should === (1)
      i.get should === (2)
      i.get should === (3)
    }
  }
}
