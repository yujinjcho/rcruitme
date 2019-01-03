package models.daos

import scala.concurrent.Future
import java.util.UUID

import anorm._
import anorm.SqlParser.get
import javax.inject.Inject
import org.joda.time.DateTime
import play.api.db.DBApi

import models.daos.AuthTokenDAO._
import models.{ AuthToken, DatabaseExecutionContext }

class AuthTokenDAO @Inject()(dbapi: DBApi, ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  def find(id: UUID) = Future {
    db.withConnection { implicit c =>

      val query: SimpleSql[Row] = SQL(
        """
          | SELECT id, userID, expiry
          | FROM auth_tokens
          | WHERE id = {id}
        """.stripMargin)
        .on("id" -> id)
      query.as(authTokenRowParser.single)
    }
  }(ec)

  def findExpired(dateTime: DateTime) = Future {
    db.withConnection { implicit c =>
      val query: SimpleSql[Row] = SQL(
        """
          | SELECT id, userID, expiry
          | FROM auth_tokens
          | WHERE expiry < TO_TIMESTAMP(${dateTime})
        """.stripMargin)
        .on("dateTime" -> dateTime.getMillis() / 1000)
      query.as(authTokenRowParser.*).flatMap(x => x)
    }
  }(ec)

  def save(token: AuthToken): Future[AuthToken] = Future {
    val authTokenId = db.withConnection { implicit conn =>
      val query = """
        | INSERT INTO auth_tokens (user_id, expiry)
        | VALUES ( {userID}, TO_TIMESTAMP({expiry}) )
      """.stripMargin
      SQL(query)
        .on("userID" -> token.userID, "expiry" -> token.expiry.getMillis() / 1000)
        .executeInsert(SqlParser.scalar[UUID].singleOpt)
    }
    token.copy(id = authTokenId.get)
  }(ec)

  def remove(id: UUID) = Future {
    val authTokenId = db.withConnection { implicit conn =>
      SQL("DELETE FROM auth_tokens WHERE id = {id}").on("id" -> id).executeUpdate()
    }
  }(ec)
}

object AuthTokenDAO {

  val authTokenRowParser: anorm.RowParser[Option[AuthToken]] = {
    get[UUID]("id") ~
    get[Int]("userID") ~
    get[DateTime]("expiry") map {
      case id~userID~expiry =>
        Some(AuthToken(id,userID,expiry))
      case _ => None
    }
  }
}
