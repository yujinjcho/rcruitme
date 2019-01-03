package models.services

import scala.concurrent.{ ExecutionContext, Future }

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import com.mohiva.play.silhouette.api.services.IdentityService
import javax.inject.Inject

import models.User
import models.daos.UserDAO

class UserService @Inject() (userDAO: UserDAO)(implicit ex: ExecutionContext) extends IdentityService[User]  {

  def retrieve(id: Int) = userDAO.find(id)

  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo)

  def save(user: User) = userDAO.save(user)

  def save(profile: CommonSocialProfile) = {
    userDAO.find(profile.loginInfo).flatMap {
      case Some(user) =>
        userDAO.save(user.copy(
          firstName = profile.firstName,
          lastName = profile.lastName,
          email = profile.email
        ))
      case None =>
        userDAO.save(User(
          userID = 0,
          credentialId = profile.loginInfo.providerID,
          firstName = profile.firstName,
          lastName = profile.lastName,
          userType = "user_type",
          email = profile.email
        ))
    }
  }
}
