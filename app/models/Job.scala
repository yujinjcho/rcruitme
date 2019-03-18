package models

import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.json.JodaWrites._

case class Job(
  id: Option[Int] = None,
  role: String,
  company: String,
  location: String,
  salary: Int,
  compensation: Option[String],
  description: String,
  benefits: Option[String],
  candidateId: Int,
  recruiterId: Int,
  viewed: Boolean = false,
  submittedAt: DateTime = DateTime.now
)

object Job {
  implicit val writes = Json.writes[Job]
}
