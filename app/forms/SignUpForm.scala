package forms

import play.api.data.Form
import play.api.data.Forms._

object SignUpForm {

  val form = Form(
    mapping(
      "userType" -> nonEmptyText,
      "email" -> email,
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "password" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  case class Data(
    userType: String,
    email: String,
    firstName: String,
    lastName: String,
    password: String
  )
}
