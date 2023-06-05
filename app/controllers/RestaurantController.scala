package controllers

import models.{Location, Restaurant}
import org.mongodb.scala.{Document, _}
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json._
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.ExecutionContext


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
        val restaurants = documents.map(document => getRestaurant(document))
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
        val restaurants = documents.map(document => getRestaurant(document))
        Ok(Json.toJson(restaurants))
      })
      .recover {
        case ex: Exception => InternalServerError(s"An error occurred: ${ex.getMessage}")
      }
  }


  def searchRestaurantsNearby(longitude: Double, latitude: Double): Action[AnyContent]
  = Action.async { implicit request: Request[AnyContent] =>

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
      val restaurants = documents.map(document => getRestaurant(document))
      Ok(Json.toJson(restaurants))
    })
      .recover {
        case ex: Exception => InternalServerError(s"An error occurred: ${ex.getMessage}")
      }
  }

  private def getRestaurant(document: Document) = {
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
      Location(document.getString("lat").toDouble, document.getString("lon").toDouble)
    )
  }
}

