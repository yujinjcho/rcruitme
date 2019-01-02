package models.daos

import scala.concurrent.Future

import java.util.UUID
import javax.inject.Inject

import anorm.{ ~, SQL, SqlParser, SimpleSql, Row}
import anorm.SqlParser.get
import play.api.db.DBApi
import org.joda.time.DateTime

import models.daos.AuthTokenDAO._
import models.AuthToken
import models.DatabaseExecutionContext


class AuthTokenDAO @Inject()(dbapi: DBApi, ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  // TODO: Figure out if this is necessary
  // private val parameters = Macro.toParameters[AuthToken]

  def find(id: UUID) = Future {
    db.withConnection { implicit c =>

      val query: SimpleSql[Row] = SQL(
        """
          |select id, userID, expiry
          |FROM auth_tokens
          |WHERE id = {id}
        """.stripMargin)
        .on("id" -> id)
      query.as(authTokenRowParser.single)
    }
  }(ec)


  def findExpired(dateTime: DateTime) = Future {
    db.withConnection { implicit c =>

      val query: SimpleSql[Row] = SQL(
        """
          |select id, userID, expiry
          |FROM auth_tokens
          |WHERE expiry < TO_TIMESTAMP(${dateTime})
        """.stripMargin)
        .on("dateTime" -> dateTime.getMillis() / 1000)
      query.as(authTokenRowParser.*).flatMap(x => x)
    }
  }(ec)

  def save(token: AuthToken): Future[AuthToken] = Future {
    val authTokenId = db.withConnection { implicit conn =>
      val query = s"""
        insert into auth_tokens
          (
            user_id,
            expiry
          )
        values
          (
            '${token.userID}',
            '${token.expiry}'
          )
        """
      SQL(query).executeInsert(SqlParser.scalar[UUID].singleOpt)
    }
    token.copy(id = authTokenId.get)
  }(ec)

  def remove(id: UUID) = Future {
    val authTokenId = db.withConnection { implicit conn =>
      val rowsUpdated: Int = SQL("""
        delete from auth_tokens
        where id = {id}
      """)
        .on("id" -> id)
        .executeUpdate()
      rowsUpdated
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
