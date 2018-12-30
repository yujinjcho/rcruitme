package models.daos

import com.mohiva.play.silhouette.api.LoginInfo
import models.User

import scala.concurrent.Future

trait UserDAO {

  def find(loginInfo: LoginInfo): Future[Option[User]]

  def find(userID: Int): Future[Option[User]]

  def save(user: User): Future[User]
}