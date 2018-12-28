package forms

import play.api.data._
import play.api.data.Forms._

object JobForm {
  val form = Form(
    mapping(
      "role" -> nonEmptyText,
      "company" -> nonEmptyText,
      "location" -> nonEmptyText,
      "salary" -> number,
      "compensation" -> optional(text),
      "description" -> nonEmptyText,
      "benefits" -> optional(text)
    )(Data.apply)(Data.unapply)
  )

  case class Data(
    role: String,
    company: String,
    location: String,
    salary: Int,
    compensation: Option[String],
    description: String,
    benefits: Option[String]
  )
}
