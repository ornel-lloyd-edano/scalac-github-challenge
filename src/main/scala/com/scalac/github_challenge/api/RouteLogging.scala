package com.scalac.github_challenge.api

import akka.event.Logging.LogLevel
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model.{HttpEntity, HttpRequest}
import akka.http.scaladsl.server.RouteResult.{Complete, Rejected}
import akka.http.scaladsl.server.directives.{DebuggingDirectives, LogEntry, LoggingMagnet}
import akka.http.scaladsl.server.{Directive0, RouteResult}
import akka.stream.Materializer
import akka.stream.scaladsl.Sink

import scala.concurrent.Await
import scala.concurrent.duration._

trait RouteLogging {
  implicit val mat: Materializer
  protected def responseTimeLoggingFunction(loggingAdapter: LoggingAdapter,
                                      level: LogLevel = Logging.InfoLevel)(request: HttpRequest)(response: RouteResult): Unit = {
    val entry = response match {
      case Complete(resp) =>
        val loggingString =
          s"""Request: ${request.method}:${request.uri}
             |Response: ${resp.status} ${entityAsString(resp.entity)}
             |""".stripMargin
        LogEntry(loggingString, level)
      case Rejected(reason) =>
        LogEntry(s"Rejected Reason: ${reason.mkString(",")}", level)
    }
    entry.logTo(loggingAdapter)
  }

  def logRequestResponse: Directive0 = {
    DebuggingDirectives.logRequestResult(LoggingMagnet(responseTimeLoggingFunction(_)))
  }

  protected def entityAsString(entity: HttpEntity): String = {
    val awaitable = entity.dataBytes
      .map(_.decodeString( entity.contentType.charsetOption.map(_.value).getOrElse("") )).runWith(Sink.head)
    Await.result(awaitable, 2 seconds)
  }
}
