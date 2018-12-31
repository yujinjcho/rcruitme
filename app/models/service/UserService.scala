package models.services

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import com.mohiva.play.silhouette.api.services.IdentityService

import models.User
import models.daos.UserDAO

import scala.concurrent.{ ExecutionContext, Future }


class UserService @Inject() (userDAO: UserDAO)(implicit ex: ExecutionContext) extends IdentityService[User]  {

  def retrieve(id: Int) = userDAO.find(id)

  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo)

  def save(user: User) = userDAO.save(user)

  def save(profile: CommonSocialProfile) = {
    userDAO.find(profile.loginInfo).flatMap {
      case Some(user) => // Update user with profile
        userDAO.save(user.copy(
          firstName = profile.firstName,
          lastName = profile.lastName,
          email = profile.email
        ))
      case None => // Insert a new user
        userDAO.save(User(
          userID = 0,
          credentialId = profile.loginInfo.providerID,
          firstName = profile.firstName,
          lastName = profile.lastName,
          userType = "user_type",
          email = profile.email
          // activated = true
        ))
    }
  }
}