package modules

import com.google.inject.AbstractModule
import models.daos._
import models.services._
import net.codingwell.scalaguice.ScalaModule

class BaseModule extends AbstractModule with ScalaModule {

  def configure(): Unit = {
    bind[UserDAO].to[UserDAOImpl]
    bind[UserService].to[UserServiceImpl]
  }
}
