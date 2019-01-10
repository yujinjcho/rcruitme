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

  implicit private val parameters = Macro.toParameters[User]

  def find(loginInfo: LoginInfo): Future[Option[User]] = Future {
    // Assumes providerID can only be "google" or "credentials" (password-based)
    val providerKey = if (loginInfo.providerID == "google") "google_key" else "email"
    db.withConnection { implicit c =>
      SQL(
        s"""
          | $selectUserModelFields
          | WHERE $providerKey = {providerKey}
        """.stripMargin).on("providerKey" -> loginInfo.providerKey).as(userRowParser.singleOpt)
    }
  }


  def find(userID: Int): Future[User] = Future {
    db.withConnection { implicit c =>
      SQL(
        s"""
          | $selectUserModelFields
          | WHERE id = {userId}
        """.stripMargin).on("userId" -> userID).as(userRowParser.single)
    }
  }

  def save(user: User): Future[User] = Future {
    val userId = db.withConnection { implicit conn =>
      SQL(s"""
        INSERT INTO users
          (
            first,
            last,
            email,
            type,
            google_key,
            activated
          )
        VALUES
          (
            {firstName},
            {lastName},
            {email},
            {userType},
            {googleKey},
            {activated}
          )
      """).bind(user).executeInsert()
    }
    user.copy(userID = userId.get.toInt)
  }

  def update(user: User): Future[User] = Future {
    val rowsUpdated : Int = db.withConnection { implicit conn =>
      SQL("""
        UPDATE users
          SET
            first = {firstName},
            last = {lastName},
            type = {userType},
            google_key = {googleKey},
            activated = {activated}
          WHERE
            id = {userID}
      """).bind(user).executeUpdate()
    }
    user
  }


  def findByEmail(email: String): Future[Option[User]] = Future {
    db.withConnection { implicit c =>
      SQL(
        s"""
          | $selectUserModelFields
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
    get[String]("userType") ~
    get[Boolean]("activated") map {
      case id~first~last~email~googleKey~userType~activated =>
        User(id, first, googleKey, last, userType, email, activated)
      // case _ => should throw some exception here?
    }
  }

  val selectUserModelFields =
    """
      | SELECT
      |   id,
      |   first,
      |   last,
      |   email,
      |   type AS userType,
      |   google_key AS googleKey,
      |   activated
      | FROM users
     """.stripMargin
}
