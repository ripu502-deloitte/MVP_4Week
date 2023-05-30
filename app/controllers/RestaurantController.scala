package controllers
import javax.inject.Inject
import scala.concurrent.{Await, ExecutionContext}
import scala.io.Source
import play.api.mvc._
import play.api.libs.json._
import org.mongodb.scala.{Document, _}
import org.mongodb.scala.bson._
import models.{Location, Restaurant}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters.{near, nearSphere}
import org.mongodb.scala.model.Indexes
import org.mongodb.scala.model.geojson.{Point, Position}
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}

import scala.concurrent.duration.DurationInt

class RestaurantController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  val mongoClient: MongoClient = MongoClient()
  val database: MongoDatabase = mongoClient.getDatabase("test")
  val collection: MongoCollection[Document] = database.getCollection("Restaurants")


  implicit val restaurantWrites: Writes[Restaurant] = (
    (JsPath \ "restaurantName").write[String] and
      (JsPath \ "cuisine").write[String] and
      (JsPath \ "openHours").write[String] and
      (JsPath \ "state").write[String] and
      (JsPath \ "cntyGeoid").write[String] and
      (JsPath \ "cntyName").write[String] and
      (JsPath \ "uaGeoid").write[String] and
      (JsPath \ "uaName").write[String] and
      (JsPath \ "msaGeoid").write[String] and
      (JsPath \ "msaName").write[String] and
      (JsPath \ "lon").write[String] and
      (JsPath \ "lat").write[String] and
      (JsPath \ "frequency").write[String] and
      (JsPath \ "isChain").write[String] and
      (JsPath \ "location").write[Location]

    ) (unlift(Restaurant.unapply))


  implicit val restaurantSeqWrites: Writes[Seq[Restaurant]] = Writes.seq(restaurantWrites)

  def searchByState(state: String): Action[AnyContent] = Action.async { implicit request =>
    val query = Document("state" -> state)
    collection.find(query).limit(5).toFuture()
      .map(documents => {
        val restaurants = documents.map(document => {
          Restaurant(
            document.getString("restaurantName"),
            document.getString("cuisine"),
            document.getString("openHours"),
            document.getString("state"),
            document.getString("cntyGeoid"),
            document.getString("cntyName"),
            document.getString("uaGeoid"),
            document.getString("uaName"),
            document.getString("msaGeoid"),
            document.getString("msaName"),
            document.getString("lon"),
            document.getString("lat"),
            document.getString("frequency"),
            document.getString("isChain"),
            Location(2.2, 2.4)
          )
        })
        Ok(Json.toJson(restaurants))
      })
      .recover {
        case ex: Exception => InternalServerError(s"An error occurred: ${ex.getMessage}")
      }
  }

  def SearchRestaurantsNearby(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    //    val minDistanceMiles = 0
    //
    //    //    val queryFilter: Bson = nearSphere("location", long, minDistanceMiles / 3963.2)
    //    //
    //    //    val query = collection.find(queryFilter)
    //
    //    val query = collection.find(near("location", new Point(new Position(-81.100044, 29.309608)), Some(minDistanceMiles / 3963.2), Some(2 / 3963.2)))
    //    val result = Await.result(query.toFuture(), 10.seconds)
    //    result.foreach(println)
    //    Ok("hello")
    val longitude: Double = -76.855606
    val latitude: Double = 38.11933
    val maxDistance: Double = 10000.00000 // Maximum distance in meters

    collection.createIndex(Indexes.geo2dsphere("location")).toFuture().foreach { indexName =>
      println(s"Created geospatial index: $indexName")
    }

    val query: Document = Document("location" -> Document(
      "$nearSphere" -> Document(
        "$geometry" -> Document(
          "type" -> "Point",
          "coordinates" -> List(longitude, latitude)
        ),
        "$maxDistance" -> maxDistance
      )
    ))

    val result: FindObservable[Document] = collection.find(query)

    result.subscribe(new Observer[Document] {
      override def onNext(result: Document): Unit = {
        // Process each matching document here
        println(result.toJson())
      }

      override def onError(e: Throwable): Unit = {
        println("Error: " + e.getMessage)
      }

      override def onComplete(): Unit = {
        println("Query completed")
        mongoClient.close() // Close the MongoDB client when done
      }
    })
    Ok("hello")
  }
}

