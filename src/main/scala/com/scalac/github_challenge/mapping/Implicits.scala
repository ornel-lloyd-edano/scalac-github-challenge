package com.scalac.github_challenge.mapping

import com.scalac.github_challenge.service.model
import com.scalac.github_challenge.api.dto

object Implicits {

  implicit class ServiceToApiContribution(val arg: model.Contribution) extends AnyVal {
    def toApi = dto.Contribution(name = arg.contributor.name, contributions = arg.numContributions)
  }

}
