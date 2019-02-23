package models.daos

import anorm._
import anorm.SqlParser.scalar
import javax.inject.Inject
import play.api.db.DBApi
import com.mohiva.play.silhouette.api.{AuthInfo, LoginInfo}
import com.mohiva.play.silhouette.password.BCryptSha256PasswordHasher
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.api.util.PasswordInfo

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PasswordAuthInfoDAO @Inject()(
  dbapi: DBApi
) extends DelegableAuthInfoDAO[PasswordInfo] {

  private val db = dbapi.database("default")

  def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    val password: Option[String] = db.withConnection { implicit c =>
      SQL("SELECT password FROM users WHERE email = {email}")
        .on("email" -> loginInfo.providerKey)
        .as(scalar[String].singleOpt)
    }
    Future.successful(password.map(p => PasswordInfo(new BCryptSha256PasswordHasher().id, p)))
  }

  def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    update(loginInfo, authInfo)
    Future.successful(authInfo)
  }

  def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    val rowsUpdated : Int = db.withConnection { implicit conn =>
      SQL("""
        UPDATE users
          SET
            password = {password}
          WHERE
            email = {email}
      """).on("password" -> authInfo.password, "email" -> loginInfo.providerKey).executeUpdate()
    }
    Future.successful(authInfo)
  }

  def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = update(loginInfo, authInfo)

  def remove(loginInfo: LoginInfo): Future[Unit] = {
     val rowsUpdated : Int = db.withConnection { implicit conn =>
      SQL("""
        UPDATE users
          SET
            password = NULL
          WHERE
            email = {email}
      """).on( "email" -> loginInfo.providerKey).executeUpdate()
    }
    Future.successful(())
  }
}
