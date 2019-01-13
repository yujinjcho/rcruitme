package controllers

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{Success,Failure}

import com.mohiva.play.silhouette.api._
import javax.inject.Inject
import com.mohiva.play.silhouette.api.crypto.CrypterAuthenticatorEncoder
import com.mohiva.play.silhouette.impl.authenticators.{JWTAuthenticator, JWTAuthenticatorSettings}
import play.api.i18n.I18nSupport
import play.api.mvc.{ AbstractController, ControllerComponents }

import models.services.UserService
import utils.auth.DefaultEnv

class ActivateAccountController @Inject() (
  cc: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  settings: JWTAuthenticatorSettings,
  authenticatorEncoder: CrypterAuthenticatorEncoder,
  )(implicit ex: ExecutionContext
) extends AbstractController(cc) with I18nSupport {

  def activate(token: String, redirect: String) = silhouette.UnsecuredAction.async {
    JWTAuthenticator.unserialize(token, authenticatorEncoder, settings) match {
      case Success(authenticator) =>
        userService.retrieve(authenticator.loginInfo).flatMap {
          case Some(user) =>
            userService.update(user.copy(activated = true)).map { _ =>
              Redirect(redirect)
            }
          case None =>
            Future.successful(Ok("user not found"))
        }
      case Failure(_) =>
        Future.successful(Ok("issues with activation link"))
    }
  }
}
