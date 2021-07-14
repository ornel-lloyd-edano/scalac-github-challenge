package com.scalac.github_challenge.service

import com.scalac.github_challenge.service.model.Failures.ServiceFailure
import com.scalac.github_challenge.service.model.{Contribution, Contributor, Organization, Repository}

import scala.concurrent.Future

trait DevContributionService {
  def getOrganizations(limit: Option[Int]): Future[Either[ServiceFailure, Seq[Organization]]]
  def getRepos(organization: Organization, limit: Option[Int]): Future[Either[ServiceFailure, Seq[Repository]]]
  def getContributors(organization: Organization, numRepos: Option[Int]): Future[Either[ServiceFailure, Seq[Contribution]]]
}
