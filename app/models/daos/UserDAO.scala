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
    db.withConnection { implicit c =>
      val email = loginInfo.providerKey
      SQL(
        """
          | SELECT id, first, last, email, type AS userType, credential_id AS credentialId
          | FROM users
          | WHERE email = {email}
        """.stripMargin).on("email" -> email).as(userRowParser.singleOpt)
    }
  }

  def find(userID: Int): Future[User] = Future {
    db.withConnection { implicit c =>
      SQL(
        """
          | SELECT id, first, last, email, type AS userType, credential_id AS credentialId
          | FROM user
          | WHERE id = {userId}
        """.stripMargin).on("userId" -> userID).as(userRowParser.single)
    }
  }

  def save(user: User): Future[User] = Future {
    val userId = db.withConnection { implicit conn =>
      user match {
        case User(_, first, credentialId, last, userType, email) =>
          SQL(s"""
            INSERT INTO USERS
              (
                first,
                last,
                email,
                type,
                credential_id
              )
            VALUES
              (
                {first},
                {last},
                {email},
                {userType},
                {credentialId}
              )
          """)
            .on(
              "first" -> first,
              "last" -> last,
              "email" -> email,
              "userType" -> userType,
              "credentialId" -> credentialId).executeInsert()
      }
    }
    user.copy(userID = userId.get.toInt)
  }
}

object UserDAO {

  val userRowParser: RowParser[User] = {
    get[Int]("id") ~
    get[String]("first") ~
    get[String]("last") ~
    get[String]("email") ~
    get[String]("credentialId") ~
    get[String]("userType") map {
      case id~first~last~email~credentialId~userType => {
        User(id, first, credentialId, last, userType, email)}
      // case _ => should throw some exception here?
    }
  }
}
