package controllers

import scala.concurrent.Future

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{ AbstractController, AnyContent, ControllerComponents, Request }

import forms.SignUpForm
import models.services.{ AuthTokenService, UserService }
import models.{ DatabaseExecutionContext, User}
import utils.auth.DefaultEnv

class SignUpController @Inject() (
  cc: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  passwordHasherRegistry: PasswordHasherRegistry,
  authInfoRepository: AuthInfoRepository,
  authTokenService: AuthTokenService,
  )(implicit ex: DatabaseExecutionContext)
  extends AbstractController(cc) with I18nSupport {

  def view = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(views.html.signUp(SignUpForm.form)))
  }

  def submit() = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    SignUpForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signUp(form))),
      data => {
        val redirect = Redirect(routes.SignUpController.view())
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
        userService.retrieve(loginInfo).flatMap {
          case Some(user) =>
            Future.successful(redirect.flashing("info" -> "Account exists already"))
          case None =>
            val authInfo = passwordHasherRegistry.current.hash(data.password)
            val user = User(
              userID = 0,
              firstName = Some(data.firstName),
              lastName = Some(data.lastName),
              credentialId = loginInfo.providerID,
              userType = "user_type_placeholder",
              email = Some(data.email)
            )
            for {
              user <- userService.save(user)
              authInfo <- authInfoRepository.add(loginInfo, authInfo)
              authToken <- authTokenService.create(user.userID)
            } yield {
              // TODO: send activation email
              silhouette.env.eventBus.publish(SignUpEvent(user, request))
              redirect.flashing("info" -> "Email confirmation has been sent")
            }
        }
      }
    )
  }
}