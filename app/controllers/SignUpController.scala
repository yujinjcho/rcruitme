package controllers

import scala.concurrent.Future

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import play.api.i18n.{ I18nSupport, Messages }
import play.api.libs.mailer.{ Email, MailerClient }
import play.api.mvc.{ AbstractController, AnyContent, ControllerComponents, Request }

import forms.SignUpForm
import models.services.UserService
import models.{ DatabaseExecutionContext, User}
import utils.auth.DefaultEnv

class SignUpController @Inject() (
  cc: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  passwordHasherRegistry: PasswordHasherRegistry,
  authInfoRepository: AuthInfoRepository,
  mailerClient: MailerClient,
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
          case Some(user) if user.googleKey.isDefined =>
            val authInfo = passwordHasherRegistry.current.hash(data.password)
            for {
              authInfo <- authInfoRepository.add(loginInfo, authInfo)
            } yield {
              redirect.flashing("info" -> "Synced to existing account")
            }
          case Some(user) =>
            Future.successful(redirect.flashing("info" -> "Account exists already"))
          case None =>
            val authInfo = passwordHasherRegistry.current.hash(data.password)
            val user = User(
              userID = 0,
              firstName = data.firstName,
              lastName = data.lastName,
              userType = "user_type_placeholder",
              email = data.email
            )
            for {
              user <- userService.save(user)
              authInfo <- authInfoRepository.add(loginInfo, authInfo)
            } yield {
              // TODO: send activation email
              val url = "test"
              mailerClient.send(Email(
                subject = Messages("email.sign.up.subject"),
                from = Messages("email.from"),
                to = Seq(data.email),
                bodyText = Some(views.txt.emails.signUp(user, url).body),
                bodyHtml = Some(views.html.emails.signUp(user, url).body)
              ))
              silhouette.env.eventBus.publish(SignUpEvent(user, request))
              redirect.flashing("info" -> "Email confirmation has been sent")
            }
        }
      }
    )
  }
}
