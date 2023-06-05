package controllers

import models.{Location, Restaurant}
import org.mongodb.scala.{Document, _}
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc._
import scalaj.http.{Http, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


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
    val query = Document("state" -> state.toUpperCase())
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
  def searchByCuisine(cuisine: String): Action[AnyContent] = Action.async { implicit request =>
    val query = Document("cuisine" -> cuisine.toLowerCase().capitalize)
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



  def SearchRestaurantsNearby(longitude:Double, latitude: Double): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>

    val maxDistance: Double = 5000.00000 // Maximum distance in meters


    val query: Document = Document("location" -> Document(
      "$nearSphere" -> Document(
        "$geometry" -> Document(
          "type" -> "Point",
          "coordinates" -> List(longitude, latitude)
        ),
        "$maxDistance" -> maxDistance
      )
    ))

    collection.find(query).toFuture().map(documents => {
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
          Location(document.getString("lat").toDouble, document.getString("lon").toDouble))
      })
      Ok(Json.toJson(restaurants))
    })
      .recover {
        case ex: Exception => InternalServerError(s"An error occurred: ${ex.getMessage}")
      }
  }

  def calculateDistance(userLat: Double, userLon: Double)= Action { implicit request: Request[AnyContent] =>

    val directionsUrl = s"https://api.mapbox.com/directions/v5/mapbox/driving/$userLon,$userLat;-96.73262,46.875854?approaches = curb;curb&access_token=pk.eyJ1IjoiYWFuY2hhbDAxIiwiYSI6ImNsaWlpNHFsZjAwY28zZG1menU4c29jbzAifQ.IBJUQCFFAOs6BM1bPrEk6Q"

    Future {
      val response: HttpResponse[String] = Http(directionsUrl).asString
      val json = Json.parse(response.body)
      println(json)
      val distance = (json \ "routes" \\ "distance").headOption.map(_.as[Double])
      distance.getOrElse(0.0)
    }
    Ok("hi")
  }
}

