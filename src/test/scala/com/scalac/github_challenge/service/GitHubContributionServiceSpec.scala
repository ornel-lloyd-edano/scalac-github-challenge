package com.scalac.github_challenge.service

import com.scalac.github_challenge.service.model.Failures.{ExternalCallError, ServiceFailure}
import com.scalac.github_challenge.service.model.{Organization}
import com.scalac.github_challenge.util.{AsyncHttpClient, Configs, ExecutionContextProvider, ExecutionContexts}
import org.scalamock.scalatest.AsyncMockFactory
import org.scalatest.flatspec.AsyncFlatSpec
import org.scalatest.matchers.should.Matchers
import spray.json.JsValue

import scala.concurrent.Future

class GitHubContributionServiceSpec extends AsyncFlatSpec with Matchers with AsyncMockFactory  {

  implicit val ec: ExecutionContextProvider = ExecutionContexts
  val mockHttpClient = mock[AsyncHttpClient]
  val gitHubService = new GitHubContributionService(mockHttpClient, Configs)

  "GitHubContributionService" should "return list of organizations if successful" in {
    val uri = Configs.getStringConfigVal("github.organizations.url").get

    val mockResult = Seq(Organization("Amazon"), Organization("Apple"), Organization("Facebook"), Organization("Microsoft"),  Organization("Netflix") )

    (mockHttpClient.getRequest[ServiceFailure, Seq[Organization]] (_:String, _:Map[String, String])
      (_: JsValue=>Seq[Organization], _: String=>ExternalCallError) (_: ExecutionContextProvider) )
      .expects(uri, Map[String, String](), *, *, *).returning(Future.successful(Right(mockResult)))
    val futureResult = gitHubService.getOrganizations(None)

    val expected = Right(Seq(Organization("Amazon"), Organization("Apple"), Organization("Facebook"), Organization("Microsoft"), Organization("Netflix") ))
    futureResult map { result=>
      result shouldBe expected
    }
  }

  "GitHubContributionService" should "return a service failure if not successful" in {
    val uri = Configs.getStringConfigVal("github.organizations.url").get

    val mockResult = ExternalCallError("The external api is not available.")
    (mockHttpClient.getRequest (_:String, _:Map[String, String])
    (_: JsValue=>Seq[Organization], _: String=> ExternalCallError) (_: ExecutionContextProvider)  )
      .expects(uri, Map[String, String](), *, *, *).returning(Future.successful(Left(mockResult)))
    val futureResult = gitHubService.getOrganizations(None)

    val expected = Left(mockResult)
    futureResult map { result=>
      result shouldBe expected
    }
  }

}
