package models

import anorm._
import anorm.JodaParameterMetaData._
import org.joda.time.DateTime

case class Job(
  id: Option[Int] = None,
  role: String,
  company: String,
  compensation: Int,
  location: String,
  description: String,
  benefits: Option[String],
  viewed: Boolean = false,
  submittedAt: DateTime = DateTime.now
)

object Job {
  implicit def toParameters: ToParameterList[Job] =
    Macro.toParameters[Job]
}
