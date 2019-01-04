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
          | SELECT id, first, last, email, type AS userType
          | FROM users
          | WHERE email = {email}
        """.stripMargin).on("email" -> email).as(userRowParser.singleOpt)
    }
  }

  def find(userID: Int): Future[User] = Future {
    db.withConnection { implicit c =>
      SQL(
        """
          | SELECT id, first, last, email, type AS userType
          | FROM user
          | WHERE id = {userId}
        """.stripMargin).on("userId" -> userID).as(userRowParser.single)
    }
  }

  def save(user: User): Future[User] = Future {
    val userId = db.withConnection { implicit conn =>
      user match {
        case User(_, Some(first), credentialId, Some(last), userType, Some(email)) =>
          SQL(s"""
            INSERT INTO USERS
              (
                first,
                last,
                email,
                type
              )
            VALUES
              (
                {first},
                {last},
                {email},
                {userType}
              )
          """)
            .on("first" -> first, "last" -> last, "email" -> email, "userType" -> userType)
            .executeInsert()
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
    get[String]("userType") map {
      case id~first~last~email~userType => {
        User(id, Some(first), "credential_id", Some(last), userType, Some(email))}
      // case _ => should throw some exception here?
    }
  }
}
