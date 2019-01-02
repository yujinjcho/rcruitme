package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.{ Clock, Credentials }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import forms.SignUpForm
import play.api.Configuration
import play.api.i18n.{ I18nSupport, Messages }
import play.api.mvc.{ AbstractController, AnyContent, ControllerComponents, Request }
import play.api.libs.mailer.{ Email, MailerClient }
import utils.auth.DefaultEnv


import models.services.{ AuthTokenService, UserService }
import models.User
import models.DatabaseExecutionContext
import forms.SignInForm

import scala.concurrent.duration._
import scala.concurrent.Future

class SignUpController @Inject() (
                                   components: ControllerComponents,
                                   silhouette: Silhouette[DefaultEnv],
                                   userService: UserService,
                                   passwordHasherRegistry: PasswordHasherRegistry,
                                   authInfoRepository: AuthInfoRepository,
                                   authTokenService: AuthTokenService,
                                   mailerClient: MailerClient
                                   // credentialsProvider: CredentialsProvider,
                                   // socialProviderRegistry: SocialProviderRegistry,
                                   // configuration: Configuration,
                                   // clock: Clock
                                 )(
                                   implicit ex: DatabaseExecutionContext
                                 ) extends AbstractController(components) with I18nSupport {

  def view = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(views.html.signUp(SignUpForm.form)))
  }

  def submit() = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    SignUpForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signUp(form))),
      data => {
        val redirect = Redirect(routes.SignUpController.view()).flashing("info" -> Messages("sign.up.email.sent", data.email))
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
        userService.retrieve(loginInfo).flatMap {
          case Some(user) =>
            val url = routes.SignInController.view().absoluteURL()

            // mailerClient.send(Email(
            //   subject = Messages("email.already.signed.up.subject"),
            //   from = Messages("email.from"),
            //   to = Seq(data.email),
            //   bodyText = Some(views.txt.emails.alreadySignedUp(user, url).body),
            //   bodyHtml = Some(views.html.emails.alreadySignedUp(user, url).body)
            // ))
            Future.successful(redirect)
          case _ =>
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
              // val url = routes.ActivateAccountController.activate(authToken.id).absoluteURL()
              // mailerClient.send(Email(
              //   subject = Messages("email.sign.up.subject"),
              //   from = Messages("email.from"),
              //   to = Seq(data.email),
              //   bodyText = Some(views.txt.emails.signUp(user, url).body),
              //   bodyHtml = Some(views.html.emails.signUp(user, url).body)
              // ))

              silhouette.env.eventBus.publish(SignUpEvent(user, request))
              redirect
            }
        }
      }
    )
  }
}
