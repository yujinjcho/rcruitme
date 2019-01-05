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

  def save(profile: CommonSocialProfile): Future[User] = profile match {
    // Assumes social profile (google) will have name and email
    case CommonSocialProfile(loginInfo, Some(first), Some(last), _, Some(email), _) =>
      userDAO.findByEmail(email).flatMap {
        case Some(user) =>
          userDAO.update(user.copy(googleKey = Some(loginInfo.providerKey)))
        case None =>
          userDAO.save(User(
            userID = 0,
            googleKey = Some(loginInfo.providerKey),
            firstName = first,
            lastName = last,
            userType = "user_type",
            email = email
          ))
      }
  }

  def update(user: User): Future[User] = userDAO.update(user)
}
