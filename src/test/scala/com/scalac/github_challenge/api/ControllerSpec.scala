package com.scalac.github_challenge.api

import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import com.scalac.github_challenge.service.DevContributionService
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import com.scalac.github_challenge.service.model.{Contribution, Contributor, Failures, Organization, Repository}
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration._

class ControllerSpec extends AnyFlatSpec with Matchers with MockFactory with ScalatestRouteTest  with SprayJsonSupport {
  implicit def default(implicit system: ActorSystem) = RouteTestTimeout(5.seconds)

  val mockContributionService = mock[DevContributionService]
  val controller = new Controller(mockContributionService)

  "Controller" should "respond with a list of organizations" in {
    val mockOrgs = Seq("Google", "Apple", "Microsoft", "Netflix", "Oracle", "Scalac").map(Organization(_))
    (mockContributionService.getOrganizations _).expects(None).returning(Future.successful(Right(mockOrgs)))
    Get("/orgs") ~> controller.allRoutes ~> check {
      status should be (StatusCodes.OK)
      val expected =
        s"""
           |["Google","Apple","Microsoft","Netflix","Oracle","Scalac"]
           |""".stripMargin.parseJson
      entityAs[JsValue] should be (expected)
    }
  }

  "Controller" should "respond with a list of organizations with limit param" in {
    val mockOrgs = Seq("Google", "Apple", "Microsoft", "Netflix", "Oracle", "Scalac").map(Organization(_))
    (mockContributionService.getOrganizations _).expects(Some(3)).returning(Future.successful(Right(mockOrgs.take(3))))
    Get("/orgs?limit=3") ~> controller.allRoutes ~> check {
      status should be (StatusCodes.OK)
      val expected =
        s"""
           |["Google","Apple","Microsoft"]
           |""".stripMargin.parseJson
      entityAs[JsValue] should be (expected)
    }
  }

  "Controller" should "respond with 404 NotFound if an organization is not found" in {
    (mockContributionService.getContributors (_: Organization, _: Option[Int]))
      .expects(Organization("microscope"), None).returning(Future.successful(Left(Failures.OrganizationNotFound("microscope"))))

    Get("/orgs/microscope/contributors") ~> controller.allRoutes ~> check {
      status should be (StatusCodes.NotFound)
      val expected = "Organization [microscope] was not found."
      entityAs[String] should be (expected)
    }
  }

  "Controller" should "respond with a list of repos" in {
    val mockRepos = Seq("repo1", "repo2", "repo3", "repo4").map(Repository)
    (mockContributionService.getRepos (_: Organization, _: Option[Int]))
      .expects(Organization("microsoft"), None).returning(Future.successful(Right(mockRepos)))

    Get("/orgs/microsoft/repos") ~> controller.allRoutes ~> check {
      status should be (StatusCodes.OK)
      val expected =
        s"""
           |["repo1", "repo2", "repo3", "repo4"]
           |""".stripMargin.parseJson
      entityAs[JsValue] should be (expected)
    }
  }

  "Controller" should "respond with a list of contributors under a given organization" in {
    val mockContributors = Seq(
      Contribution(Contributor("Bill"), Organization("microsoft"), 999),
      Contribution(Contributor("Steve"), Organization("microsoft"), 200),
      Contribution(Contributor("Zuck"), Organization("microsoft"), 60),
      Contribution(Contributor("Buffet"), Organization("microsoft"), 10)
    )
    (mockContributionService.getContributors (_: Organization, _: Option[Int]))
      .expects(Organization("microsoft"), None).returning(Future.successful(Right(mockContributors)))

    Get("/orgs/microsoft/contributors") ~> controller.allRoutes ~> check {
      status should be (StatusCodes.OK)
      val expected =
        s"""
           |[{"name": "Bill", "contributions": 999},
           |{"name": "Steve", "contributions": 200},
           |{"name": "Zuck", "contributions": 60},
           |{"name": "Buffet", "contributions": 10}]
           |""".stripMargin.parseJson
      entityAs[JsValue] should be (expected)
    }
  }

}
