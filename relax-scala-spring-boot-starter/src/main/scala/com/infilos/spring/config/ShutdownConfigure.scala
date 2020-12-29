package com.infilos.spring.config

import com.infilos.spring.utils._
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextClosedEvent

import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import scala.collection.mutable.ArrayBuffer

class ShutdownConfigure extends ApplicationListener[ContextClosedEvent] with Loggable {

  @PostConstruct
  def construct(): Unit = {
    // setup io-thread-pool shutdown hook
    ShutdownConfigure.register(() => {
      try {
        Executors.IOThreadPool.shutdown()
        if (!Executors.IOThreadPool.awaitTermination(30, TimeUnit.SECONDS)) {
          log.warn("IO thread pool shutdown gracefully failed.")
        } else {
          log.warn("IO thread pool shutdown gracefully succed.")
        }
      } catch {
        case _: Throwable => Thread.currentThread().interrupt()
      }
    })

    // setup db-thread-pool shutdown hook
    ShutdownConfigure.register(() => {
      try {
        Executors.DBThreadPool.shutdown()
        if (!Executors.IOThreadPool.awaitTermination(30, TimeUnit.SECONDS)) {
          log.warn("DB thread pool shutdown gracefully failed.")
        } else {
          log.warn("DB thread pool shutdown gracefully succed.")
        }
      } catch {
        case _: Throwable => Thread.currentThread().interrupt()
      }
    })
  }

  override def onApplicationEvent(e: ContextClosedEvent): Unit = {
    ShutdownConfigure.shutdowns.foreach { task =>
      try {
        task.run()
      } catch {
        case _: Throwable => Thread.currentThread().interrupt()
      }
    }
  }
}

object ShutdownConfigure {
  private val shutdowns: ArrayBuffer[Runnable] = ArrayBuffer.empty

  def register(task: Runnable): Unit = synchronized {
    shutdowns.append(task)
  }
}
