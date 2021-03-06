package com.infilos.spring.utils

import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.{ExecutorService, ThreadFactory, Executors => JExecutors}
import scala.concurrent.ExecutionContext

object Executors {

  lazy val IOThreadPool: ExecutorService =
    JExecutors.newCachedThreadPool(new ThreadFactory() {
      private final val counter = new AtomicLong(0)

      override def newThread(r: Runnable): Thread = {
        val thread = new Thread(r)
        thread.setName("io-" + counter.getAndIncrement)
        thread.setDaemon(true)
        thread
      }
    })

  lazy val IODispatcher: ExecutionContext = new ExecutionContext() {

    private final val executor = IOThreadPool

    override def execute(runnable: Runnable): Unit =
      executor.submit(runnable)

    override def reportFailure(cause: Throwable): Unit =
      cause.printStackTrace()
  }

  lazy val DBThreadPool: ExecutorService =
    JExecutors.newCachedThreadPool(new ThreadFactory() {
      private final val counter = new AtomicLong(0)

      override def newThread(r: Runnable): Thread = {
        val thread = new Thread(r)
        thread.setName("db-" + counter.getAndIncrement)
        thread.setDaemon(true)
        thread
      }
    })

  lazy val DBDispatcher: ExecutionContext = new ExecutionContext() {

    private final val executor = DBThreadPool

    override def execute(runnable: Runnable): Unit =
      executor.submit(runnable)

    override def reportFailure(cause: Throwable): Unit =
      cause.printStackTrace()
  }
}
