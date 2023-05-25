package modules

import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import com.google.inject.AbstractModule
import models.Restaurant
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.mvc.Results.InternalServerError

import scala.io.Source

class injection  extends AbstractModule with AkkaGuiceSupport {


  override def configure(): Unit = {

    val mongoClient: MongoClient = MongoClient()
    val database: MongoDatabase = mongoClient.getDatabase("test")
    val collection: MongoCollection[Document] = database.getCollection("Restaurants")

    val csvFile = "/home/svinayakamnigam/Downloads/chainness_point_2021_part3.csv"
    val source = Source.fromFile(csvFile)
    val lines = source.getLines().toList
    source.close()

    val header :: rows = lines
    val restaurants = rows.map(row => {
      val values = row.split(",").map(_.trim)
      Restaurant(
        values(0),
        values(1),
        values(2),
        values(3),
        values(4),
        values(5),
        values(6),
        values(7),
        values(8),
        values(9),
        values(10),
        values(11),
        values(12),
        values(13)
      )
    })

    val documents = restaurants.map(restaurant => {
      Document(
        "restaurantName" -> restaurant.restaurantName,
        "cuisine" -> restaurant.cuisine,
        "openHours" -> restaurant.openHours,
        "state" -> restaurant.state,
        "cntyGeoid" -> restaurant.cntyGeoid,
        "cntyName" -> restaurant.cntyName,
        "uaGeoid" -> restaurant.uaGeoid,
        "uaName" -> restaurant.uaName,
        "msaGeoid" -> restaurant.msaGeoid,
        "msaName" -> restaurant.msaName,
        "lon" -> restaurant.lon,
        "lat" -> restaurant.lat,
        "frequency" -> restaurant.frequency,
        "isChain" -> restaurant.isChain
      )
    })

    collection.insertMany(documents).toFuture()
  }

}