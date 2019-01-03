package models.daos

import scala.concurrent.Future

import anorm._
import anorm.SqlParser.get
import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.Inject
import play.api.db.DBApi

import models.{ DatabaseExecutionContext, User }
import models.daos.UserDAO._

class UserDAO @Inject()(dbapi: DBApi, ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  def find(loginInfo: LoginInfo) = Future {
    val user: Option[User] = db.withConnection { implicit c =>
      val email = loginInfo.providerKey
      val result = SQL(
        s"""
          | SELECT id, first, last, email, type AS userType
          | FROM users
          | WHERE email = {email}
        """.stripMargin).on("email" -> email).as(userRowParser.singleOpt)
      result
    }
    user
  }(ec)

  def find(userID: Int) = Future {
    db.withConnection { implicit c =>
      SQL(
        """
          | SELECT id, first, last, email, type AS userType
          | FROM user
          | WHERE id = {userId}
        """.stripMargin)
        .on("userId" -> userID)
        .as(userRowParser.single)
    }
  }(ec)

  def save(user: User): Future[User] = Future {
    val userId = db.withConnection { implicit conn =>
      user match {
        case User(_, Some(first), credentialId, Some(last), userType, Some(email)) =>
          val query = s"""
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
          """
          SQL(query)
            .on("first" -> first, "last" -> last, "email" -> email, "userType" -> userType)
            .executeInsert()
      }
    }
    user.copy(userID = userId.get.toInt)
  }(ec)
}

object UserDAO {

  val userRowParser: anorm.RowParser[User] = {
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
