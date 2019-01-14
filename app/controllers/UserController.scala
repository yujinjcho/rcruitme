package controllers

import scala.concurrent.Future

import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.Silhouette
import javax.inject._
import play.api.libs.json._
import play.api.mvc._

import models.User
import utils.auth.DefaultEnv

class UserController @Inject() (
  cc: ControllerComponents,
  silhouette: Silhouette[DefaultEnv]
) extends AbstractController(cc) {

  implicit val userWrite = new Writes[User] {
    def writes(user: User) = Json.obj(
      "id" -> user.userID,
      "firstName" -> user.firstName,
      "lastName" -> user.lastName,
      "email" -> user.email,
      "type" -> user.userType.toString,
      "activated" -> user.activated
    )
  }

  def get = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    val user: User = request.identity
    Future.successful(Ok(Json.toJson(user)))
  }

}
