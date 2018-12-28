package models.services

import anorm._
import javax.inject._
import play.api.db.DBApi
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import models.Job

@Singleton
class JobService @Inject()(dbapi: DBApi) {

  private val db = dbapi.database("default")

  def insert(job: Job): Future[Option[Long]] = Future {
    db.withConnection { implicit conn =>
      SQL("""
        insert into jobs
          (role, company, compensation, location, description, benefits, viewed, submitted_at)
          values
          ({role}, {company}, {compensation}, {location}, {description}, {benefits}, {viewed}, {submittedAt})
      """).bind(job).executeInsert()
    }
  }

}
