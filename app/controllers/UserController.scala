package controllers

import scala.concurrent.{ ExecutionContext, Future }

import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.Silhouette
import javax.inject._
import play.api.i18n.I18nSupport
import play.api.libs.json._
import play.api.mvc._

import forms.UpdateUserForm
import models.{ User, UserType }
import models.services.UserService
import utils.auth.DefaultEnv

class UserController @Inject() (
  cc: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService
)(implicit ex: ExecutionContext) extends AbstractController(cc) with I18nSupport {

  def get = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    val user: User = request.identity
    Future.successful(Ok(Json.toJson(user)))
  }

  def update = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    UpdateUserForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(form.errorsAsJson)),
      data => {
        val user: User = request.identity
        userService.update(user.copy(
          firstName = data.firstName.getOrElse(user.firstName),
          lastName = data.lastName.getOrElse(user.lastName),
          userType = UserType.withName(data.userType.getOrElse(user.userType.toString)),
          email = data.email.getOrElse(user.email)
        )).map { updatedUser =>
          Ok(Json.toJson(user))
        }
      }
    )
  }

}
