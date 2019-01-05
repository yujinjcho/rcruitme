package models.daos

import scala.concurrent.Future

import anorm._
import anorm.SqlParser.get
import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.{Inject, Singleton}
import play.api.db.DBApi

import models.{ DatabaseExecutionContext, User }
import models.daos.UserDAO._

@Singleton
class UserDAO @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  def find(loginInfo: LoginInfo): Future[Option[User]] = Future {
    // Assumes providerID can only be "google" or "credentials" (password-based)
    val providerKey = if (loginInfo.providerID == "google") "google_key" else "email"
    db.withConnection { implicit c =>
      SQL(
        s"""
          | SELECT id, first, last, email, type AS userType, google_key AS googleKey
          | FROM users
          | WHERE $providerKey = {providerKey}
        """.stripMargin).on("providerKey" -> loginInfo.providerKey).as(userRowParser.singleOpt)
    }
  }


  def find(userID: Int): Future[User] = Future {
    db.withConnection { implicit c =>
      SQL(
        """
          | SELECT id, first, last, email, type AS userType, google_key AS googleKey
          | FROM user
          | WHERE id = {userId}
        """.stripMargin).on("userId" -> userID).as(userRowParser.single)
    }
  }

  def save(user: User): Future[User] = Future {
    val userId = db.withConnection { implicit conn =>
      user match {
        case User(_, first, googleKey, last, userType, email) =>
          SQL(s"""
            INSERT INTO users
              (
                first,
                last,
                email,
                type,
                google_key
              )
            VALUES
              (
                {first},
                {last},
                {email},
                {userType},
                {googleKey}
              )
          """)
            .on(
              "first" -> first,
              "last" -> last,
              "email" -> email,
              "userType" -> userType,
              "googleKey" -> googleKey).executeInsert()
      }
    }
    user.copy(userID = userId.get.toInt)
  }

  def update(user: User): Future[User] = Future {
    val userId = db.withConnection { implicit conn =>
      user match {
        case User(userId, first, googleKey, last, userType, email) =>
          SQL(s"""
            UPDATE users
              SET
                first = {first},
                last = {last},
                type = {userType},
                google_key = {googleKey}
              WHERE
                id = {userId}
          """)
            .on(
              "first" -> first,
              "last" -> last,
              "userType" -> userType,
              "googleKey" -> googleKey,
              "userId" -> userId
            ).executeInsert()
      }
    }
    user.copy(userID = userId.get.toInt)
  }


  def findByEmail(email: String): Future[Option[User]] = Future {
    db.withConnection { implicit c =>
      SQL(
        s"""
          | SELECT id, first, last, email, type AS userType, google_key AS googleKey
          | FROM users
          | WHERE email = {email}
        """.stripMargin).on("email" -> email).as(userRowParser.singleOpt)
    }
  }
}

object UserDAO {

  val userRowParser: RowParser[User] = {
    get[Int]("id") ~
    get[String]("first") ~
    get[String]("last") ~
    get[String]("email") ~
    get[Option[String]]("googleKey") ~
    get[String]("userType") map {
      case id~first~last~email~googleKey~userType =>
        User(id, first, googleKey, last, userType, email)
      // case _ => should throw some exception here?
    }
  }
}
