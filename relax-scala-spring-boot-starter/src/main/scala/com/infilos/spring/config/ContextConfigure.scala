package com.infilos.spring.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct

@Component
class ContextConfigure {

  @Autowired
  private val context: ApplicationContext = null

  @PostConstruct
  def construct(): Unit = {
    ContextConfigure._Configure = this
  }
}

object ContextConfigure {
  private var _Configure: ContextConfigure = _

  def inject[T](beanClass: Class[T]): T = context.getBean(beanClass)

  def context: ApplicationContext = {
    if (isSpringRunning) {
      _Configure.context
    } else {
      throw new UnsupportedOperationException("Spring context is unavailable!")
    }
  }

  def isSpringRunning: Boolean = _Configure != null

  def inject[T](beanName: String): T = context.getBean(beanName).asInstanceOf[T]
}
