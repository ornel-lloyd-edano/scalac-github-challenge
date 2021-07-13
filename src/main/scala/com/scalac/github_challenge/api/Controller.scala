package com.scalac.github_challenge.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import com.scalac.github_challenge.api.format.JsonFormat
import com.scalac.github_challenge.service.DevContributionService
import com.scalac.github_challenge.service.model.{Contributor, Organization}

import scala.util.{Failure, Success}
import com.scalac.github_challenge.service.model.Failures.{ContributorNotFound, OrganizationNotFound}
import com.scalac.github_challenge.mapping.Implicits._

class Controller(contributionService: DevContributionService) extends JsonFormat with SprayJsonSupport {
  import spray.json._

  val routes = pathPrefix("org") {
    parameter("limit".as[Option[Int]]) { (limit)=>
      get {
        onComplete(contributionService.getOrganizations(limit)) {
          case Success(Right(result))=>
            complete(StatusCodes.OK, HttpEntity(ContentTypes.`application/json`, result.map(_.name).toJson.prettyPrint))
          case Failure(exception: Exception)=>
            complete(StatusCodes.InternalServerError, exception.getMessage)
        }
      } ~
      path(Segment / "contributors") { orgName=>
        get {
          onComplete(contributionService.getContributors(Organization(orgName), limit)) {
            case Success(Right(result))=>
              complete(StatusCodes.OK, HttpEntity(ContentTypes.`application/json`, result.map(_.name).toJson.prettyPrint))
            case Success(Left(OrganizationNotFound(name)))=>
              complete(StatusCodes.NotFound, s"Organization [$name] was not found.")
            case Failure(exception: Exception)=>
              complete(StatusCodes.InternalServerError, exception.getMessage)
          }
        }
      }
    } ~
    path(Segment / "contributors" / Segment) { (orgName, devName)=>
      get {
        onComplete(contributionService.getContributions(Contributor(devName), Organization(orgName))) {
          case Success(Right(result))=>
            complete(StatusCodes.OK, HttpEntity(ContentTypes.`application/json`, result.toApi.toJson.prettyPrint))
          case Success(Left(OrganizationNotFound(name)))=>
            complete(StatusCodes.NotFound, s"Organization [$name] was not found.")
          case Success(Left(ContributorNotFound(name)))=>
            complete(StatusCodes.NotFound, s"Contributor [$name] was not found.")
          case Failure(exception: Exception)=>
            complete(StatusCodes.InternalServerError, exception.getMessage)
        }
      }
    }
  }

}
