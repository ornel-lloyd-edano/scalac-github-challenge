package com.scalac.github_challenge.util

import org.slf4j.{LoggerFactory}

trait Logging {

  val logger = LoggerFactory.getLogger(this.getClass)

}
