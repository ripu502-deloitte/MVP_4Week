package modules

import com.google.inject.AbstractModule
import com.opencsv.CSVReader
import models.{Location, Restaurant}
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}
import play.api.libs.concurrent.AkkaGuiceSupport

import java.io.{File, FileReader}
import scala.io.Source

class injection extends AbstractModule with AkkaGuiceSupport {


  def loadCSVData(filePath: String, batchSize: Int): Unit = {
    val mongoClient: MongoClient = MongoClient()
    val database: MongoDatabase = mongoClient.getDatabase("test")
    val collection: MongoCollection[Document] = database.getCollection("Restaurants")

    val csvFile = new File(filePath)
    val csvReader = new CSVReader(new FileReader(csvFile))


    try {
      var count = 0
      var rows = Seq[Restaurant]()

      var line: Array[String] = csvReader.readNext()
      line = csvReader.readNext()

      while (line != null) {
        val location=Location(line(10).toDouble,line(11).toDouble)
        val user = Restaurant(
          line(0),
          line(1),
          line(2),
          line(3),
          line(4),
          line(5),
          line(6),
          line(7),
          line(8),
          line(9),
          line(10),
          line(11),
          line(12),
          line(13),
          location
        )

        rows = rows :+ user
        count += 1

        if (count % batchSize == 0) {
          val documents = rows.map(restaurant =>
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
              "isChain" -> restaurant.isChain,
            "location" -> Document(
                "latitude" -> restaurant.location.latitude,
                "longitude" -> restaurant.location.longitude
              )
            )
          )
          collection.insertMany(documents).toFuture()
          rows = Seq[Restaurant]()
        }

        line = csvReader.readNext()
      }

      if (rows.nonEmpty) {
        val documents = rows.map(restaurant =>
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
            "isChain" -> restaurant.isChain,
            "location" -> Document(
              "latitude" -> restaurant.location.latitude,
              "longitude" -> restaurant.location.longitude
            )
          )
        )
        collection.insertMany(documents).toFuture()
      }
    } finally {
      csvReader.close()
      mongoClient.close()
    }
  }


  override def configure(): Unit = {

    loadCSVData("/home/surabroy/Downloads/merge-csv.com__64647fa425e55.csv", 500)
  }

}