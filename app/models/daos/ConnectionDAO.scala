package models.daos

import anorm._
import javax.inject._
import models.DatabaseExecutionContext
import play.api.db.DBApi

import scala.concurrent.Future

@Singleton
class ConnectionDAO @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  def create(candidateId: Int, recruiterId: Int): Future[Option[Long]] = Future {
    db.withConnection { implicit conn =>
      SQL("""
        INSERT INTO connections
          (
            candidate_id,
            recruiter_id
          )
        VALUES
          (
            {candidate_id},
            {recruiter_id}
          )
      """).on("candidate_id" -> candidateId, "recruiter_id" -> recruiterId).executeInsert()
    }
  }
}
