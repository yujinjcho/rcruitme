package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.{ Clock, Credentials }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import forms.SignInForm
// import models.services.UserService
// import net.ceedubs.ficus.Ficus._
import play.api.Configuration
import play.api.i18n.{ I18nSupport, Messages }
import play.api.mvc.{ AbstractController, AnyContent, ControllerComponents, Request }
import utils.auth.DefaultEnv

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

class SignInController @Inject() (
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
    Future.successful(Ok(views.html.signIn(SignInForm.form)))
  }

  def submit = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok("TEST"))
  }

}