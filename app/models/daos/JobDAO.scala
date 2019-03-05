package models.daos

import anorm._
import anorm.JodaParameterMetaData._
import javax.inject._
import play.api.db.DBApi
import scala.concurrent.Future

import models.DatabaseExecutionContext
import models.Job

@Singleton
class JobDAO @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  implicit private val parameters = Macro.toParameters[Job]

  def create(job: Job): Future[Option[Long]] = Future {
    db.withConnection { implicit conn =>
      SQL("""
        INSERT INTO jobs
          (
            role,
            company,
            location,
            salary,
            compensation,
            description,
            benefits,
            viewed,
            submitted_at,
            candidate_id,
            recruiter_id
          )
        VALUES
          (
            {role},
            {company},
            {location},
            {salary},
            {compensation},
            {description},
            {benefits},
            {viewed},
            {submittedAt},
            {candidateId},
            {recruiterId}
          )
      """).bind(job).executeInsert()
    }
  }
}
