package controllers

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.{ Clock, Credentials }
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._

import javax.inject.Inject
import net.ceedubs.ficus.Ficus._
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.libs.json._
import play.api.mvc.{ AbstractController, AnyContent, ControllerComponents, Request }

import forms.SignInForm
import models.services.UserService
import utils.auth.DefaultEnv

class SignInController @Inject() (
  cc: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  clock: Clock,
  configuration: Configuration,
  userService: UserService,
  credentialsProvider: CredentialsProvider,
  socialProviderRegistry: SocialProviderRegistry,
)(implicit ex: ExecutionContext) extends AbstractController(cc) with I18nSupport {

  implicit val socialProviderWrite = new Writes[SocialProvider] {
    def writes(provider: SocialProvider) = Json.obj(
      "id" -> JsString(provider.id),
      "href" -> JsString(configuration.underlying.as[String](s"silhouette.${provider.id}.redirectURL"))
    )
  }

  def view = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(Json.obj(
      "providers" -> Json.toJson(socialProviderRegistry.providers)
    )))
  }

  def submit() = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    SignInForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(Json.obj(
        "providers" -> Json.toJson(socialProviderRegistry.providers),
        "errors" -> form.errorsAsJson
      ))),
      data => {
        val credentials = Credentials(data.email, data.password)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          userService.retrieve(loginInfo).flatMap {
            // TODO: handle non-activated users
            case Some(user) =>
              val c = configuration.underlying
              silhouette.env.authenticatorService.create(loginInfo)
                .map { authenticator =>
                  if (!data.rememberMe)
                    authenticator
                  else
                    authenticator.copy(
                      expirationDateTime = clock.now + c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry"),
                      idleTimeout = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout")
                    )}
                .flatMap { authenticator =>
                  silhouette.env.eventBus.publish(LoginEvent(user, request))
                  silhouette.env.authenticatorService.init(authenticator).flatMap { v =>
                    silhouette.env.authenticatorService.embed(v, Ok(Json.obj(
                      "result" -> "success"
                    )))
                  }
              }
            case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
          }
        }.recover {
          case _: ProviderException =>
            BadRequest(Json.obj(
              "providers" -> Json.toJson(socialProviderRegistry.providers),
              "errors" -> "Invalid credentials!"
            ))
        }
      }
    )
  }

}
