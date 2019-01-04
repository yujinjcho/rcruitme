package models.services

import scala.concurrent.{ ExecutionContext, Future }

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import com.mohiva.play.silhouette.api.services.IdentityService
import javax.inject.Inject

import models.User
import models.daos.UserDAO

class UserService @Inject() (userDAO: UserDAO)(implicit ex: ExecutionContext) extends IdentityService[User]  {

  def retrieve(id: Int): Future[User] = userDAO.find(id)

  def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo)

  def save(user: User): Future[User] = userDAO.save(user)

  def save(profile: CommonSocialProfile): Future[User] = {
    userDAO.find(profile.loginInfo).flatMap { user =>
      (user, profile) match {
        // Assumes social profile (google) will have name and email
        case (Some(u), CommonSocialProfile(_, Some(first), Some(last), _, Some(email), _)) =>
          userDAO.save(u.copy(firstName = first, lastName = last, email = email))
        case (None, CommonSocialProfile(loginInfo, Some(first), Some(last), _, Some(email), _)) =>
          userDAO.save(User(
            userID = 0,
            credentialId = loginInfo.providerID,
            firstName = first,
            lastName = last,
            userType = "user_type",
            email = email
          ))
      }
    }
  }
}
