package forms

import play.api.data._
import play.api.data.Forms._

object JobForm {
  val form = Form(
    mapping(
      "role" -> nonEmptyText,
      "company" -> nonEmptyText,
      "compensation" -> number,
      "location" -> nonEmptyText,
      "description" -> nonEmptyText,
      "benefits" -> optional(text)
    )(Data.apply)(Data.unapply)
  )

  case class Data(
    role: String,
    company: String,
    compensation: Int,
    location: String,
    description: String,
    benefits: Option[String]
  )
}
