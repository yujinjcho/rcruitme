package controllers

import scala.concurrent.{ ExecutionContext, Future }

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.state.UserStateItem
import javax.inject.Inject
import play.api.i18n.{ I18nSupport, Messages }
import play.api.libs.json._
import play.api.libs.mailer.{ Email, MailerClient }
import play.api.mvc.{ AbstractController, AnyContent, ControllerComponents, Request }

import models.services.UserService
import utils.auth.DefaultEnv

class SocialAuthController @Inject() (
    cc: ControllerComponents,
    silhouette: Silhouette[DefaultEnv],
    userService: UserService,
    authInfoRepository: AuthInfoRepository,
    socialProviderRegistry: SocialProviderRegistry,
    mailerClient: MailerClient,
  )(implicit ex: ExecutionContext)
  extends AbstractController(cc) with I18nSupport with Logger {

  def authenticate(provider: String, redirect: Option[String], jobRedirect: Option[String]) = Action.async { implicit request: Request[AnyContent] =>
    (socialProviderRegistry.get[SocialStateProvider](provider) match {
      case Some(p: SocialStateProvider with CommonSocialProfileBuilder) =>
        p.authenticate(UserStateItem(Map("redirect" -> redirect.getOrElse(""), "jobRedirect" -> jobRedirect.getOrElse("")))).flatMap {
          case Left(result) => Future.successful(result)
          case Right(StatefulAuthInfo(authInfo, userState)) => for {
            profile <- p.retrieveProfile(authInfo)
            user <- userService.save(profile)
            _ <- authInfoRepository.save(profile.loginInfo, authInfo)
            authenticator <- silhouette.env.authenticatorService.create(profile.loginInfo)
            token <- silhouette.env.authenticatorService.init(authenticator)
            result <- Future.successful(
              Redirect(s"${userState.state("redirect")}?token=$token&redirect=${userState.state("jobRedirect")}")
            )
          } yield {

            val url = routes.ActivateAccountController.activate(token, userState.state("redirect")).absoluteURL()
            if (!user.activated) {
              mailerClient.send(Email(
                subject = Messages("email.sign.up.subject"),
                from = Messages("email.from"),
                to = Seq(user.email),
                bodyText = Some(views.txt.emails.signUp(user, url).body),
                bodyHtml = Some(views.html.emails.signUp(user, url).body)
              ))
            }
            silhouette.env.eventBus.publish(LoginEvent(user, request))
            result
          }
        }
      case _ => Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider $provider"))
    }).recover {
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        BadRequest(Json.obj("errors" -> Messages("invalid.credentials")))
    }
  }

}
