package modules

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Flow, Framing}
import akka.util.ByteString
import models.{Location, Restaurant}
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}
import org.mongodb.scala.model.Indexes
import org.mongodb.scala.result.InsertOneResult

import java.nio.file.Paths
import java.util.concurrent.Executors
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

@Singleton
class AddtoMongodatabaseUsingStreams {



    val collection=DB.collection




  def toRestaurantModel(line: List[String]): Restaurant = {
    val id = java.util.UUID.randomUUID().toString
    Restaurant(
      _id = id,
      line.head,
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
      Location(line(10).toDouble, line(11).toDouble)
    )
  }

  val mappingFlow: Flow[String, Restaurant, NotUsed] = Flow[String].map(line => {
    toRestaurantModel(line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)").toList)
  })

  def insertDataToTables(restaurant: Restaurant): Future[InsertOneResult] = {
    collection.insertOne(getMongoDocument(restaurant)).toFuture()
  }

  private def getMongoDocument(restaurant: Restaurant) = {
    Document(
      "_id"-> restaurant._id,
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
        "longitude" -> restaurant.location.longitude,
        "latitude" -> restaurant.location.latitude
      )
    )
  }



  val actorSystem = ActorSystem("akka_Assignment")
  implicit val materializer: ActorMaterializer = ActorMaterializer()(actorSystem)

  val executorService = Executors.newFixedThreadPool(50)
  implicit val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(executorService)

  FileIO.fromPath(Paths.get("/home/svinayakamnigam/Downloads/merge-csv.com__64647fa425e55.csv"))
    .via(Framing.delimiter(ByteString("\n"), 4096)
      .map(_.utf8String)).drop(1)
    .via(mappingFlow)
    .mapAsync(500)(restaurant => insertDataToTables(restaurant)) // Try to change the parallelism
    .run().recover {
    case e: Exception => e.printStackTrace()
  }.onComplete(_ => {
    collection.createIndex(Indexes.geo2dsphere("location"))
      .toFuture().foreach { indexName =>
      println(s"Created geospatial index: $indexName")
    }
    println("Job Complete.")
  })

}


