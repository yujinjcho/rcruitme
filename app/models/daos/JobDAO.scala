package models.daos

import anorm._
import anorm.JodaParameterMetaData._
import javax.inject._
import play.api.db.DBApi
import scala.concurrent.Future

import models.DatabaseExecutionContext
import models.{ Job, User, UserType }
import models.daos.JobDAO._

@Singleton
class JobDAO @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  implicit private val parameters = Macro.toParameters[Job]

  def findAll(user: User): Future[Seq[Job]] = Future {
    val idName = user.userType match {
      case UserType.Candidate => "candidate_id"
      case UserType.Recruiter => "recruiter_id"
    }

    db.withConnection { implicit conn =>
      SQL"""
        #$selectJobModelFields
        FROM jobs
        WHERE #$idName = ${user.userID}
      """.as(jobRowParser *)
    }
  }

  def find(id: Int): Future[Option[Job]] = Future {
    db.withConnection { implicit conn =>
      SQL"""
        #$selectJobModelFields
        FROM jobs
        WHERE id = $id
      """.as(jobRowParser.singleOpt)
    }
  }

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

object JobDAO {
  val jobRowParser: RowParser[Job] = Macro.namedParser[Job]

  val selectJobModelFields = """
    SELECT
      id,
      role,
      company,
      location,
      salary,
      compensation,
      description,
      benefits,
      viewed,
      submitted_at as submittedAt,
      candidate_id as candidateId,
      recruiter_id as recruiterId
  """
}
