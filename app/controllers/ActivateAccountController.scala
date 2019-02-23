package controllers

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

import java.net.URI

import com.mohiva.play.silhouette.api._
import javax.inject.Inject
import com.mohiva.play.silhouette.api.crypto.CrypterAuthenticatorEncoder
import com.mohiva.play.silhouette.impl.authenticators.{JWTAuthenticator, JWTAuthenticatorSettings}
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, ControllerComponents}
import models.daos.ConnectionDAO
import models.services.UserService
import models.{User, UserType}
import utils.auth.DefaultEnv

class ActivateAccountController @Inject() (
  cc: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  userService: UserService,
  connectionDAO: ConnectionDAO,
  settings: JWTAuthenticatorSettings,
  authenticatorEncoder: CrypterAuthenticatorEncoder,
  )(implicit ex: ExecutionContext
) extends AbstractController(cc) with I18nSupport {

  def activate(token: String, redirect: String) = silhouette.UnsecuredAction.async {
    JWTAuthenticator.unserialize(token, authenticatorEncoder, settings) match {
      case Success(authenticator) =>
        userService.retrieve(authenticator.loginInfo).flatMap {
          case Some(user) =>
            createConnectionIfSharedLink(redirect, user)

            userService.update(user.copy(activated = true)).map { _ =>
              Redirect(addTokenToRedirect(redirect, token))
            }
          case None =>
            Future.successful(Ok("user not found"))
        }
      case Failure(_) =>
        Future.successful(Ok("issues with activation link"))
    }
  }

  private def createConnectionIfSharedLink(redirect: String, user: User): Unit = {
    if (user.userType == UserType.Recruiter) {
      extractCandidateId(redirect).map { candidateId =>
        userService.retrieve(candidateId).map { candidate =>
          connectionDAO.create(candidate.userID, user.userID)
        }
      }
    }
  }

  private def extractCandidateId(redirect: String): Option[Int] = {
    redirect.split("/").takeRight(2) match {
      case Array("submit-job", candidateId)  => Some(candidateId.toInt)
      case _ => None
    }
  }

  private def addTokenToRedirect(redirect: String, token: String): String = {
    val separator = if (new URI(redirect).getQuery() == null) "?" else "&"
    redirect + separator + s"token=$token"
  }
}
