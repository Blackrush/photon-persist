package org.photon.common.persist

import java.util.concurrent.Executor
import com.twitter.util.{Try, Promise, Future}

object Async {
  def apply[T](fn: => T)(implicit e: Executor): Future[T] = {
    val promise = Promise[T]
    e.execute(new Runnable {
      def run() {
        promise.update(Try(fn))
      }
    })
    promise
  }
}
