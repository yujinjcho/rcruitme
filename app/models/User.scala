package models

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }

case class User(
  userID: Int,
  firstName: String,
  googleKey: Option[String] = None,
  lastName: String,
  userType: String,
  email: String,
  activated: Boolean = false
) extends Identity {
  def loginInfo: LoginInfo = {
    googleKey match {
      case Some(key) => LoginInfo("google", key)
      case None => LoginInfo("credentials", email)
    }
  }
}
