package controllers

import javax.inject._
import play.api._
import play.api.i18n.I18nSupport
import play.api.mvc._

import forms.JobForm

class SubmitJobController @Inject()(
  cc: ControllerComponents
) extends AbstractController(cc) with I18nSupport {

  def view = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.submitJob(JobForm.form))
  }

  def submit() = Action { implicit request: Request[AnyContent] =>
    JobForm.form.bindFromRequest.fold(
      form => BadRequest(views.html.submitJob(form)),
      data => {
        Redirect(routes.SubmitJobController.view()).flashing("info" -> "Job submitted.")
      }
    )
  }

}
