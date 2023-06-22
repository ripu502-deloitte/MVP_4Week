package modules


import com.google.inject.AbstractModule

import play.api.libs.concurrent.AkkaGuiceSupport




class injection extends AbstractModule with AkkaGuiceSupport {

  override def configure(): Unit = {

    bind(classOf[AddtoMongodatabaseUsingStreams]).asEagerSingleton()
  }


}