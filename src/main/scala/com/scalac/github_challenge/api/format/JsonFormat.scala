package com.scalac.github_challenge.api.format

import com.scalac.github_challenge.api.dto.Contribution
import com.scalac.github_challenge.service.model.{GitHubContributor, GitHubOrg, GitHubRepo, Organization}
import spray.json.DefaultJsonProtocol

trait JsonFormat extends DefaultJsonProtocol {

 implicit val contributionFormat = jsonFormat2(Contribution)
 implicit val githubContributionFormat = jsonFormat3(GitHubContributor)
 implicit val githubOrgFormat = jsonFormat2(GitHubOrg)
 implicit val orgFormat = jsonFormat1(Organization)
 implicit val githubRepoFormat = jsonFormat3(GitHubRepo)
}

object JsonFormat extends JsonFormat