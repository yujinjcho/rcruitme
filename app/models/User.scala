package models

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }

case class User(
  userID: Int,
  firstName: Option[String],
  credentialId: String,
  lastName: Option[String],
  userType: String,
  email: Option[String]
) extends Identity {
  def loginInfo: LoginInfo = LoginInfo(credentialId, email.get)
}
