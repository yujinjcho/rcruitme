package models

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }

case class User(
  userID: Int,
  firstName: String,
  credentialId: String,
  lastName: String,
  userType: String,
  email: String
) extends Identity {
  def loginInfo: LoginInfo = LoginInfo(credentialId, email)
}
