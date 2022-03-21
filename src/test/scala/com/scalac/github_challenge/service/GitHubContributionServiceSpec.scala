package com.scalac.github_challenge.service

import com.scalac.github_challenge.service.model.Failures.{ExternalCallError, ServiceFailure}
import com.scalac.github_challenge.service.model.{Contribution, Contributor, Organization, Repository}
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

  "GitHubContributionService" should "return a list of organizations if successful" in {
    val uri = Configs.getStringConfigVal("github.organizations.url").get

    val mockResult = Seq("Amazon", "Apple",  "Facebook",   "Microsoft",  "Netflix").map(Organization)

    (mockHttpClient.getRequest[ServiceFailure, Seq[Organization]] (_:String, _:Map[String, String])
      (_: JsValue=>Seq[Organization], _: String=>ExternalCallError) (_: ExecutionContextProvider) )
      .expects(uri, Map[String, String](), *, *, *).returning(Future.successful(Right(mockResult)))
    val futureResult = gitHubService.getOrganizations(None)

    val expected = Right(Seq("Amazon", "Apple", "Facebook", "Microsoft", "Netflix" ).map(Organization))
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

  "GitHubContributionService" should "return a list of repositories if successful" in {
    val org = Organization("microsoft")
    val uri = Configs.getStringConfigVal("github.repositories.url").get
      .replace("{org}", org.name)

    val mockResult = Seq("repo1", "repo2", "repo3", "repo4",  "repo5").map(Repository)

    (mockHttpClient.getRequest[ServiceFailure, Seq[Repository]] (_:String, _:Map[String, String])
      (_: JsValue=>Seq[Repository], _: String=>ExternalCallError) (_: ExecutionContextProvider) )
      .expects(uri, Map[String, String](), *, *, *).returning(Future.successful(Right(mockResult)))
    val futureResult = gitHubService.getRepos(org, None)

    val expected = Right(Seq("repo1", "repo2", "repo3", "repo4", "repo5").map(Repository) )
    futureResult map { result=>
      result shouldBe expected
    }
  }

  "GitHubContributionService" should "return an ordered list of contributors (most contributions first) under a given organization if successful" in {
    val org = Organization("microsoft")
    val repoUri = Configs.getStringConfigVal("github.repositories.url").get
      .replace("{org}", org.name)

    val mockRepoResult = Seq("repo1", "repo2", "repo3").map(Repository)

    (mockHttpClient.getRequest[ServiceFailure, Seq[Repository]] (_:String, _:Map[String, String])
      (_: JsValue=>Seq[Repository], _: String=>ExternalCallError) (_: ExecutionContextProvider) )
      .expects(repoUri, Map[String, String](), *, *, *).returning(Future.successful(Right(mockRepoResult)))


    def contribUri(repo: Repository) = Configs.getStringConfigVal("github.contributors.url").get
      .replace("{org}", org.name)
      .replace("{repo}", repo.name)

    val mockContribResult1 = Seq(Contribution(Contributor("Bill"), Organization("microsoft"), 999),
      Contribution(Contributor("Zuck"), Organization("microsoft"), 100), Contribution(Contributor("Steve"), Organization("microsoft"), 300))
    (mockHttpClient.getRequest[ServiceFailure, Seq[Contribution]] (_:String, _:Map[String, String])
      (_: JsValue=>Seq[Contribution], _: String=>ExternalCallError) (_: ExecutionContextProvider) )
      .expects(contribUri(mockRepoResult(0)), Map[String, String](), *, *, *).returning(Future.successful(Right(mockContribResult1))).once()

    val mockContribResult2 = Seq(Contribution(Contributor("Bill"), Organization("microsoft"), 100),
      Contribution(Contributor("Zuck"), Organization("microsoft"), 500), Contribution(Contributor("Buffet"), Organization("microsoft"), 100))
    (mockHttpClient.getRequest[ServiceFailure, Seq[Contribution]] (_:String, _:Map[String, String])
      (_: JsValue=>Seq[Contribution], _: String=>ExternalCallError) (_: ExecutionContextProvider) )
      .expects(contribUri(mockRepoResult(1)), Map[String, String](), *, *, *).returning(Future.successful(Right(mockContribResult2))).once()

    val mockContribResult3 = Seq(Contribution(Contributor("Steve"), Organization("microsoft"), 100),
      Contribution(Contributor("Larry"), Organization("microsoft"), 250), Contribution(Contributor("Buffet"), Organization("microsoft"), 100))
    (mockHttpClient.getRequest[ServiceFailure, Seq[Contribution]] (_:String, _:Map[String, String])
      (_: JsValue=>Seq[Contribution], _: String=>ExternalCallError) (_: ExecutionContextProvider) )
      .expects(contribUri(mockRepoResult(2)), Map[String, String](), *, *, *).returning(Future.successful(Right(mockContribResult3))).once()

    val futureResult = gitHubService.getContributors(org, None)

    val expected = Right(Seq(
      Contribution(Contributor("Bill"), Organization("microsoft"), 1099),
      Contribution(Contributor("Zuck"), Organization("microsoft"), 600),
      Contribution(Contributor("Steve"), Organization("microsoft"), 400),
      Contribution(Contributor("Larry"), Organization("microsoft"), 250),
      Contribution(Contributor("Buffet"), Organization("microsoft"), 200),
    ))
    futureResult map { result=>
      result shouldBe expected
    }
  }

}
