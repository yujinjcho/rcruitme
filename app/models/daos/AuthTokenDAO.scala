package models.daos

import scala.concurrent.Future
import java.util.UUID

import anorm._
import anorm.SqlParser.get
import javax.inject.{ Inject, Singleton }
import org.joda.time.DateTime
import play.api.db.DBApi

import models.daos.AuthTokenDAO._
import models.{ AuthToken, DatabaseExecutionContext }

@Singleton
class AuthTokenDAO @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  def find(id: UUID): Future[Option[AuthToken]] = Future {
    db.withConnection { implicit c =>
      SQL(
        """
          | SELECT id, userID, expiry
          | FROM auth_tokens
          | WHERE id = {id}
        """.stripMargin).on("id" -> id).as(authTokenRowParser.single)
    }
  }

  def findExpired(dateTime: DateTime): Future[List[AuthToken]] = Future {
    db.withConnection { implicit c =>
      SQL(
        """
          | SELECT id, userID, expiry
          | FROM auth_tokens
          | WHERE expiry < TO_TIMESTAMP(${dateTime})
        """.stripMargin)
        .on("dateTime" -> dateTime.getMillis() / 1000)
        .as(authTokenRowParser.*).flatten
    }
  }

  def save(token: AuthToken): Future[AuthToken] = Future {
    val authTokenId = db.withConnection { implicit conn =>
      SQL("""
        | INSERT INTO auth_tokens (user_id, expiry)
        | VALUES ( {userID}, TO_TIMESTAMP({expiry}) )
      """.stripMargin)
        .on("userID" -> token.userID, "expiry" -> token.expiry.getMillis() / 1000)
        .executeInsert(SqlParser.scalar[UUID].singleOpt)
    }
    token.copy(id = authTokenId.get)
  }

  def remove(id: UUID): Future[Unit] = Future {
    val authTokenId = db.withConnection { implicit conn =>
      SQL("DELETE FROM auth_tokens WHERE id = {id}").on("id" -> id).executeUpdate()
    }
  }
}

object AuthTokenDAO {

  val authTokenRowParser: RowParser[Option[AuthToken]] = {
    get[UUID]("id") ~
    get[Int]("userID") ~
    get[DateTime]("expiry") map {
      case id~userID~expiry =>
        Some(AuthToken(id,userID,expiry))
      case _ => None
    }
  }
}
