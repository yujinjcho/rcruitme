package models

import com.mohiva.play.silhouette.api.{ Identity, LoginInfo }
import play.api.libs.json._

case class User(
  userID: Int,
  firstName: String,
  googleKey: Option[String] = None,
  lastName: String,
  userType: UserType.Type,
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

object User {
  implicit val writes = new Writes[User] {
    def writes(user: User) = Json.obj(
      "id" -> user.userID,
      "firstName" -> user.firstName,
      "lastName" -> user.lastName,
      "email" -> user.email,
      "type" -> user.userType.toString,
      "activated" -> user.activated
    )
  }
}

object UserType extends Enumeration {
  type Type = Value;

  val None = Value("none");
  val Candidate = Value("candidate");
  val Recruiter = Value("recruiter");
}
