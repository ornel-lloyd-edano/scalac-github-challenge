package com.scalac.github_challenge.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import com.scalac.github_challenge.service.DevContributionService
import com.scalac.github_challenge.service.model.{Contributor, Organization}

import scala.util.{Failure, Success}
import com.scalac.github_challenge.service.model.Failures.{ContributorNotFound, OrganizationNotFound}


class Controller(contributionService: DevContributionService) extends SprayJsonSupport {

  val routes = pathPrefix("org") {
    parameter("limit".as[Option[Int]]) { (limit)=>
      get {
        onComplete(contributionService.getOrganizations(limit)) {
          case Success(Right(result))=>
            complete(StatusCodes.OK)
          case Failure(exception: Exception)=>
            complete(StatusCodes.InternalServerError)
        }
      } ~
      path(Segment / "contributors") { orgName=>
        get {
          onComplete(contributionService.getContributors(Organization(orgName), limit)) {
            case Success(Right(result))=>
              complete(StatusCodes.OK)
            case Success(Left(OrganizationNotFound(name)))=>
              complete(StatusCodes.NotFound )
            case Failure(exception: Exception)=>
              complete(StatusCodes.InternalServerError)
          }
        }
      }
    } ~
    path(Segment / "contributors" / Segment) { (orgName, devName)=>
      get {
        onComplete(contributionService.getContributions(Contributor(devName), Organization(orgName))) {
          case Success(Right(result))=>
            complete(StatusCodes.OK)
          case Success(Left(OrganizationNotFound(name)))=>
            complete(StatusCodes.NotFound )
          case Success(Left(ContributorNotFound(name)))=>
            complete(StatusCodes.NotFound )
          case Failure(exception: Exception)=>
            complete(StatusCodes.InternalServerError)
        }
      }
    }
  }

}
