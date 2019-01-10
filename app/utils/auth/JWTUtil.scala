package utils.auth

import com.mohiva.play.silhouette.api.crypto.CrypterAuthenticatorEncoder
import com.mohiva.play.silhouette.impl.authenticators.{JWTAuthenticator, JWTAuthenticatorSettings}
import javax.inject.Inject
import scala.util.{Success,Failure}

class JWTUtil @Inject() (
  settings: JWTAuthenticatorSettings,
  authenticatorEncoder: CrypterAuthenticatorEncoder) {

  def unserialize(token: String): Option[JWTAuthenticator] = {
    JWTAuthenticator.unserialize(token, authenticatorEncoder, settings) match {
      case Success(authenticator) => Some(authenticator)
      case Failure(f) => None
    }
  }
}
