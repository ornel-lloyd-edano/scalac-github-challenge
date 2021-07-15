package com.scalac.github_challenge.api

import akka.http.scaladsl.server.Route
import com.github.swagger.akka.SwaggerHttpService
import com.scalac.github_challenge.api.dto.Contribution
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{ArraySchema, Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import javax.ws.rs.{GET, Path, Produces}
import javax.ws.rs.core.MediaType

@Path("/orgs")
trait DocumentedController extends SwaggerHttpService  {

  override def apiClasses: Set[Class[_]] = Set(classOf[DocumentedController])

  @GET
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(summary = "Return the list of organizations in github",
    responses = Array(
      new ApiResponse(responseCode = "200", description = "alphabetically ordered list of organizations",
        content = Array(new Content( array = new ArraySchema(schema = new Schema(implementation = classOf[String] )) )) ),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def getOrganizationsRoute: Route

  @GET
  @Path("/{organization}/repos")
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(summary = "Return the list of repositories of an organization in github",
    responses = Array(
      new ApiResponse(responseCode = "200", description = "alphabetically ordered list of repositories of an organization",
        content = Array(new Content( array = new ArraySchema(schema = new Schema(implementation = classOf[String] )) )) ),
      new ApiResponse(responseCode = "404", description = "Given organization is not found"),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def getReposRoute: Route

  @GET
  @Path("/{organization}/contributors")
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(summary = "Return the list of contributors of an organization in github",
    responses = Array(
      new ApiResponse(responseCode = "200", description = "list of repository contributors under an organization ordered by number of contributions (highest to lowest)",
        content = Array(new Content( array = new ArraySchema(schema = new Schema(implementation = classOf[Contribution] )) )) ),
      new ApiResponse(responseCode = "404", description = "Given organization is not found"),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def getContributorsRoute: Route

  //just an alias to SwaggerHttpService.routes
  lazy val documentationRoutes = routes

}
