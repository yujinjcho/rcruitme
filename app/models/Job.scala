package models

import org.joda.time.DateTime

case class Job(
  id: Option[Int] = None,
  role: String,
  company: String,
  location: String,
  salary: Int,
  compensation: Option[String],
  description: String,
  benefits: Option[String],
  viewed: Boolean = false,
  submittedAt: DateTime = DateTime.now
)
