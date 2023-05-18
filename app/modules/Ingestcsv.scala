package modules

import Models.Restaurant
import Repositories.RestaurantRepository
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

import javax.inject.Inject
import scala.io.Source

class Ingestcsv extends AbstractModule with AkkaGuiceSupport{

  override def configure(): Unit = {
    bind(classOf[Ingest]).asEagerSingleton()
  }

}
