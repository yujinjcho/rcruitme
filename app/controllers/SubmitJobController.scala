package controllers

import javax.inject._
import play.api._
import play.api.i18n.I18nSupport
import play.api.mvc._
import scala.concurrent.{ ExecutionContext, Future }

import forms.JobForm
import models.Job
import models.daos.JobDAO

class SubmitJobController @Inject()(
  cc: ControllerComponents,
  jobDAO: JobDAO
)(implicit ec: ExecutionContext) extends AbstractController(cc) with I18nSupport {

  def view = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.submitJob(JobForm.form))
  }

  def submit() = Action.async { implicit request: Request[AnyContent] =>
    JobForm.form.bindFromRequest.fold(
      form => Future(BadRequest(views.html.submitJob(form))),
      data => {
        val job = Job(
          role = data.role,
          company = data.company,
          location = data.location,
          salary = data.salary,
          compensation = data.compensation,
          description = data.description,
          benefits = data.benefits
        )
        jobDAO.create(job).map { _ =>
          Redirect(routes.SubmitJobController.view()).flashing("info" -> "Job submitted.")
        }
      }
    )
  }

}
