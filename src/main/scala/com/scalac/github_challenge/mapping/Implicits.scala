package com.scalac.github_challenge.mapping

import com.scalac.github_challenge.service.model
import com.scalac.github_challenge.api.dto
import com.scalac.github_challenge.service.model.Contributor

object Implicits {

  implicit class ServiceToApiContribution(val arg: model.Contribution) extends AnyVal {
    def toApi = dto.Contribution(name = arg.contributor.name, contributions = arg.numContributions)
  }

  implicit class ApiToServiceContribution(val arg: dto.Contribution) extends AnyVal {
    def toService = model.Contribution(contributor = Contributor(arg.name),
      organization = None, numContributions = arg.contributions)
  }
}
