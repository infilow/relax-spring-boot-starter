package com.infilos.spring.utils

import org.slf4j._

trait Loggable {
  def log: Logger = LoggerFactory.getLogger(this.getClass)
}
