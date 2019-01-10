package models

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }

case class User(
  userID: Int,
  firstName: String,
  googleKey: Option[String] = None,
  lastName: String,
  userType: UserType.Type,
  email: String
) extends Identity {
  def loginInfo: LoginInfo = {
    googleKey match {
      case Some(key) => LoginInfo("google", key)
      case None => LoginInfo("credentials", email)
    }
  }
}

object UserType extends Enumeration {
  type Type = Value;

  val None = Value("none");
  val Candidate = Value("candidate");
  val Recruiter = Value("recruiter");
}
