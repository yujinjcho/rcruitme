package models.daos

import anorm.{ ~, Macro, SQL, SimpleSql, Row}
import anorm.SqlParser.get
import javax.inject.Inject
import play.api.db.DBApi
import com.mohiva.play.silhouette.api.LoginInfo

import models.User
import models.daos.UserDAO._
import models.DatabaseExecutionContext

import scala.concurrent.Future

class UserDAO @Inject()(dbapi: DBApi, ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")
  private val parameters = Macro.toParameters[User]

  def find(loginInfo: LoginInfo) = Future {
    val user: Option[User] = db.withConnection { implicit c =>
      val email = loginInfo.providerKey
      val result = SQL(
        s"""
          |select id, first, last, email, type AS userType
          |FROM users
          |WHERE email = '${email}'
        """.stripMargin).as(userRowParser.singleOpt)
      result
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
      user match {
        case User(_, Some(first), credentialId, Some(last), userType, Some(email)) =>
          val query = s"""
            insert into users
              (
                first,
                last,
                email,
                type
              )
            values
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
