package com.scalac.github_challenge.util

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.unmarshalling.Unmarshal

import scala.concurrent.Future
import spray.json._
class AkkaHttpClient(implicit as: ActorSystem) extends AsyncHttpClient with Logging with SprayJsonSupport {

  override def getRequest[L, R](uri: String, headers: Map[String, String])(success: JsValue=> R, failure: String=> L)(implicit ec: ExecutionContextProvider): Future[Either[L, R]] = {
    val requestHeaders = scala.collection.immutable.Seq(headers.toArray:_*)
        .map(entry=> RawHeader(entry._1, entry._2))
    Http().singleRequest(HttpRequest(uri = uri, headers = requestHeaders))
        .flatMap {
          case response @ HttpResponse(StatusCodes.OK, _, _, _)=>
            Unmarshal(response).to[JsValue].map(v=>Right(success(v)))(ec.cpuBoundExCtx)
          case response=>
            val errorMsg = s"${Unmarshal(response).to[String]}"
            logger.error(errorMsg)
            Future.successful(Left(failure(errorMsg)))
        }(ec.ioBoundExCtx)
  }
}
