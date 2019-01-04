package controllers

import scala.concurrent.Future

import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.LogoutEvent
import com.mohiva.play.silhouette.api.Silhouette
import javax.inject._
import play.api.mvc._

import utils.auth.DefaultEnv

class HomeController @Inject()(
  cc: ControllerComponents,
  silhouette: Silhouette[DefaultEnv]
) extends AbstractController(cc) {

  def index = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    Future.successful(Ok(views.html.index()))
  }

  def hello(name: String) = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.hello(name))
  }

  def google = Action { implicit request: Request[AnyContent] =>
    Ok("google authenticated")
  }

  def signOut = silhouette.SecuredAction.async { implicit request: SecuredRequest[DefaultEnv, AnyContent] =>
    val result = Redirect(routes.SignInController.view())
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, result)
  }
}
