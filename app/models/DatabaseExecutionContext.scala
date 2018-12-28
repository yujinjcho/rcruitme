package models

import akka.actor.ActorSystem
import javax.inject._
import play.api.libs.concurrent.CustomExecutionContext

@Singleton
class DatabaseExecutionContext @Inject()(
  system: ActorSystem
) extends CustomExecutionContext(system, "database.dispatcher")
