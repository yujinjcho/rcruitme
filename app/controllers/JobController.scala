package controllers

import com.mohiva.play.silhouette.api.Silhouette
import javax.inject._
import play.api._
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import scala.concurrent.{ ExecutionContext, Future }

import forms.JobForm
import models.Job
import models.daos.{ConnectionDAO,JobDAO}
import utils.auth.DefaultEnv

class JobController @Inject()(
  cc: ControllerComponents,
  silhouette: Silhouette[DefaultEnv],
  jobDAO: JobDAO,
  connectionDAO: ConnectionDAO
)(implicit ec: ExecutionContext) extends AbstractController(cc) with I18nSupport {

  def submit(candidateId: String) = silhouette.SecuredAction.async { implicit request =>
    JobForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(form.errorsAsJson)),
      data => {
        connectionDAO.exists(candidateId = candidateId.toInt, recruiterId = request.identity.userID)
          .flatMap {
            case true =>
              val job = Job(
                role = data.role,
                company = data.company,
                location = data.location,
                salary = data.salary,
                compensation = data.compensation,
                description = data.description,
                benefits = data.benefits,
                candidateId = candidateId.toInt,
                recruiterId = request.identity.userID
              )
              jobDAO.create(job).map { job => Created(Json.toJson(job)) }
            case false =>
              Future.successful(BadRequest(Json.obj("errors" -> "recruiter is not connected to candidate")))
          }
      }
    )
  }
}
