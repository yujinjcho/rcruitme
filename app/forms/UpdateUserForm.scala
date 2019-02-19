package forms

import play.api.data.Form
import play.api.data.Forms._

object UpdateUserForm {

  val form = Form(
    mapping(
      "userType" -> optional(text),
      "email" -> optional(email),
      "firstName" -> optional(text),
      "lastName" -> optional(text),
      "password" -> optional(text)
    )(Data.apply)(Data.unapply)
  )

  case class Data(
    userType: Option[String],
    email: Option[String],
    firstName: Option[String],
    lastName: Option[String],
    password: Option[String]
  )
}
