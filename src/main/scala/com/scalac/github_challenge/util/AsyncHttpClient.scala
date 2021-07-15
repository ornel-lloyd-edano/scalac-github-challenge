package com.scalac.github_challenge.util

import spray.json.JsValue

import scala.concurrent.Future

trait AsyncHttpClient { //abstraction is still coupled to spray.JsValue
  def getRequest[L, R](uri: String, headers: Map[String, String])(success: JsValue=> R, failure: String=> L) (implicit ec: ExecutionContextProvider) : Future[Either[L, R]]
}
