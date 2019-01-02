package models.services

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.util.Clock
import models.AuthToken
import models.daos.AuthTokenDAO
import org.joda.time.DateTimeZone

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._
import scala.language.postfixOps

class AuthTokenService @Inject() (
                                       authTokenDAO: AuthTokenDAO,
                                       clock: Clock
                                     )(
                                       implicit
                                       ex: ExecutionContext
                                     ) {


  def create(userID: Int, expiry: FiniteDuration = 5 minutes) = {
    val token = AuthToken(UUID.randomUUID(), userID, clock.now.withZone(DateTimeZone.UTC).plusSeconds(expiry.toSeconds.toInt))
    authTokenDAO.save(token)
  }

  def validate(id: UUID) = authTokenDAO.find(id)

  def clean = authTokenDAO.findExpired(clock.now.withZone(DateTimeZone.UTC)).flatMap { tokens =>
    Future.sequence(tokens.map { token =>
      authTokenDAO.remove(token.id).map(_ => token)
    })
  }
}
