package models.daos

import anorm._
import anorm.SqlParser.{ str, int }
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import javax.inject.Inject
import play.api.db.DBApi

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SocialAuthInfoDAO @Inject()(
  dbapi: DBApi
) extends DelegableAuthInfoDAO[OAuth2Info] {

  private val db = dbapi.database("default")

  val TokenType = "Bearer"

  def find(loginInfo: LoginInfo): Future[Option[OAuth2Info]] = {
    val password: Option[(String,Int)] = db.withConnection { implicit c =>
      SQL("SELECT google_token, token_expiry FROM users WHERE email = {email}")
        .on("email" -> loginInfo.providerKey)
        .as(str("google_token") ~ int("token_expiry") map (SqlParser.flatten) singleOpt)
    }
    Future.successful(
      password.map { case (token, expiry) =>
        OAuth2Info(accessToken = token, tokenType = Some(TokenType), expiresIn = Some(expiry))}
    )
  }

  def add(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = {
    update(loginInfo, authInfo)
    Future.successful(authInfo)
  }

  def update(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = {
    val rowsUpdated : Int = db.withConnection { implicit conn =>
      SQL("""
        UPDATE users
          SET
            google_token = {password},
            token_expiry = {expiry}
          WHERE
            google_key = {googleKey}
      """).on(
        "password" -> authInfo.accessToken,
        "expiry" -> authInfo.expiresIn.getOrElse(0),
        "googleKey" -> loginInfo.providerKey
      ).executeUpdate()
    }
    Future.successful(authInfo)
  }

  def save(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = {
    find(loginInfo).flatMap {
      case Some(_) => update(loginInfo, authInfo)
      case None    => add(loginInfo, authInfo)
    }
  }

  def remove(loginInfo: LoginInfo): Future[Unit] = {
     val rowsUpdated : Int = db.withConnection { implicit conn =>
      SQL("""
        UPDATE users
          SET
            google_token = NULL,
            expiry = NULL
          WHERE
            google_key = {googleKey}
      """).on( "googleKey" -> loginInfo.providerKey).executeUpdate()
    }
    Future.successful(())
  }
}
