package controllers

import javax.inject._
import play.api.mvc._

class HomeController @Inject()(
  cc: ControllerComponents,
) extends AbstractController(cc) {

  def index = Action { implicit request =>
    Ok("Backend is ready")
  }

  def healthCheck = Action { implicit request: Request[AnyContent] =>
    Ok("Ok")
  }
}
