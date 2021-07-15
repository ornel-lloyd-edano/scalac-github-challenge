package com.scalac.github_challenge.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import com.scalac.github_challenge.api.format.JsonFormat
import com.scalac.github_challenge.service.DevContributionService
import com.scalac.github_challenge.service.model.Organization

import scala.util.{Failure, Success}
import com.scalac.github_challenge.service.model.Failures.{ExternalCallError, OrganizationNotFound}
import com.scalac.github_challenge.mapping.Implicits._
import com.scalac.github_challenge.util.ExecutionContextProvider

class Controller(contributionService: DevContributionService)(implicit val mat: Materializer) extends JsonFormat with SprayJsonSupport with RouteLogging {
  import spray.json._

  val getOrganizationsRoute: Route = parameter('limit.as[Int].optional) { (limit: Option[Int])=>
    path("orgs") {
      get {
        onComplete(contributionService.getOrganizations(limit)) {
          case Success(Right(result))=>
            complete(StatusCodes.OK, HttpEntity(ContentTypes.`application/json`, result.map(_.name).toJson.prettyPrint))
          case Success(Left(ExternalCallError(reason)))=>
            complete(StatusCodes.InternalServerError, reason)
          case Failure(exception)=>
            complete(StatusCodes.InternalServerError, exception.getMessage)
        }
      }
    }
  }

  val getReposRoute: Route = path("orgs" / Segment / "repos") { orgName=>
    parameter('limit.as[Int].optional) { (limit: Option[Int])=>
      get {
        onComplete(contributionService.getRepos(Organization(orgName), limit)) {
          case Success(Right(result))=>
            complete(StatusCodes.OK, HttpEntity(ContentTypes.`application/json`, result.map(_.name).toJson.prettyPrint))
          case Success(Left(OrganizationNotFound(name)))=>
            complete(StatusCodes.NotFound, s"Organization [$name] was not found.")
          case Success(Left(ExternalCallError(reason)))=>
            complete(StatusCodes.InternalServerError, reason)
          case Failure(exception)=>
            complete(StatusCodes.InternalServerError, exception.getMessage)
        }
      }
    }
  }

  val getContributorsRoute: Route = path("orgs" / Segment / "contributors") { orgName=>
    parameter('num_repos.as[Int].optional) { (numRepos: Option[Int])=>
      get {
        onComplete(contributionService.getContributors(Organization(orgName), numRepos)) {
          case Success(Right(result))=>
            complete(StatusCodes.OK, HttpEntity(ContentTypes.`application/json`, result.map(_.toApi).toJson.prettyPrint))
          case Success(Left(OrganizationNotFound(name)))=>
            complete(StatusCodes.NotFound, s"Organization [$name] was not found.")
          case Success(Left(ExternalCallError(reason)))=>
            complete(StatusCodes.InternalServerError, reason)
          case Failure(exception)=>
            complete(StatusCodes.InternalServerError, exception.getMessage)
        }
      }
    }
  }

  val routes: Route = logRequestResponse {
    getOrganizationsRoute ~ getReposRoute ~ getContributorsRoute
  }

}
