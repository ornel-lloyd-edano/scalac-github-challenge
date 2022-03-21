package com.scalac.github_challenge.util

trait ConfigProvider {
  def getIntConfigVal(path: String): Option[Int]
  def getStringConfigVal(path: String): Option[String]
}
