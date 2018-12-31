package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.{ Clock, Credentials }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import forms.SignUpForm
import play.api.Configuration
import play.api.i18n.{ I18nSupport, Messages }
import play.api.mvc.{ AbstractController, AnyContent, ControllerComponents, Request }
import utils.auth.DefaultEnv


import forms.SignInForm

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

class SignUpController @Inject() (
                                   components: ControllerComponents,
                                   silhouette: Silhouette[DefaultEnv],
                                   // userService: UserService,
                                   // credentialsProvider: CredentialsProvider,
                                   // socialProviderRegistry: SocialProviderRegistry,
                                   // configuration: Configuration,
                                   // clock: Clock
                                 )(
                                   implicit ex: ExecutionContext
                                 ) extends AbstractController(components) with I18nSupport {

  def view = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(views.html.signUp(SignUpForm.form)))
  }

  def submit() = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    SignUpForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signUp(form))),
      data => {
        Future.successful(Redirect(routes.HomeController.index()))
      }
    )
  }
}
