package models.daos

import anorm.{ ~, Macro, SQL, SimpleSql, Row}
import anorm.SqlParser.get
import javax.inject.Inject
import play.api.db.DBApi
import com.mohiva.play.silhouette.api.LoginInfo

import models.User
import models.daos.UserDAOImpl._
import models.DatabaseExecutionContext

import scala.concurrent.Future

class UserDAOImpl @Inject()(dbapi: DBApi, ec: DatabaseExecutionContext) extends UserDAO {

  private val db = dbapi.database("default")
  private val parameters = Macro.toParameters[User]

  def find(loginInfo: LoginInfo) = Future {
    val user = db.withConnection { implicit c =>

      val email = loginInfo.providerKey
      val query: SimpleSql[Row] = SQL(
        """
          |select id, first, last, email, type AS userType
          |FROM user
          |WHERE email = {email}
        """.stripMargin)
        .on("email" -> email)
      query.as(userRowParser.single)
    }
    user
  }(ec)


  def find(userID: Int) = Future {
    db.withConnection { implicit c =>

      val query: SimpleSql[Row] = SQL(
        """
          |select id, first, last, email, type AS userType
          |FROM user
          |WHERE id = {userId}
        """.stripMargin)
        .on("userId" -> userID)
      query.as(userRowParser.single)
    }
  }(ec)


  def save(user: User): Future[User] = Future {
    val userId = db.withConnection { implicit conn =>
      SQL("""
        insert into users
          (
            first,
            last,
            email,
            type
          )
        values
          (
            ${user.firstName},
            ${user.lastName},
            ${user.email},
            ${user.userType}
          )
      """).executeInsert()
    }
    user.copy(userID = userId.get.toInt)
  }(ec)

}

object UserDAOImpl {

  val userRowParser: anorm.RowParser[Option[User]] = {
    get[Int]("id") ~
    get[String]("first") ~
    get[String]("last") ~
    get[String]("email") ~
    get[String]("userType") map {
      case id~first~last~email~userType =>
        Some(User(id,Some(first),"credential_id",Some(last),userType,Some(email)))
      case _ => None
    }
  }
}
