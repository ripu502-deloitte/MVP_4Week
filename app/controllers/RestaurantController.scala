package controllers

import models.{Location, Restaurant}
import org.mongodb.scala.model.Filters.{equal, gte, lte, notEqual, or}
import org.mongodb.scala.model.Projections.include
import org.mongodb.scala.{Document, model, _}
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json._
import play.api.mvc._

import java.time.format.DateTimeFormatter
import java.time.{OffsetDateTime, ZoneOffset}
import javax.inject.Inject
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}


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

  def getRestaurantOpenHours(restaurantId: Int): Option[(String, String)] = {
    print("HIIIIII")
//    val query = equal("_id", restaurantId)
    val query = Document("_id" -> restaurantId)
    val result = collection
      .find(query)
      .projection(include("OpenHours"))
      .limit(1)
      .toFuture()
    print(result)
    val documents = Await.result(result, Duration.Inf)
    documents.headOption.flatMap(_.get("OpenHours").map(_.asString().getValue))
      .map(parseOpenHours)
  }

  private def parseOpenHours(openHours: String): (String, String) = {
    val Array(days, hours) = openHours.split("\\|").map(_.trim)
    (days, hours)
  }

  def isRestaurantOpen(restaurantId: Int): Boolean = {
    val openHoursOpt = getRestaurantOpenHours(restaurantId)
    print(openHoursOpt)
    openHoursOpt.exists { case (days, hours) =>
      // Get current IST time
//      val istOffset = ZoneOffset.ofHoursMinutes(5, 30)
      val estOffset = ZoneOffset.ofHours(-5)
      val currentTime = OffsetDateTime.now(estOffset)
      println("hello",currentTime)

      // Get current day and time in the required format
      val currentDay = currentTime.format(DateTimeFormatter.ofPattern("EEE")).toLowerCase
      val currentTimeStr = currentTime.format(DateTimeFormatter.ofPattern("hh:mm a"))

      // Check if restaurant is open on the current day and time
      days.contains(currentDay) && hours.contains(currentTimeStr)
    }
  }
  def checkRestaurantOpen(restaurantId: Int): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    val isOpen = isRestaurantOpen(restaurantId)
    if (isOpen) {
      Ok("The restaurant is currently open.")
    } else {
      Ok("The restaurant is currently closed.")
    }
  }
//  def isRestaurantOpen(restaurant: String): Boolean = {
//    // Get current IST time
//    val istOffset = ZoneOffset.ofHoursMinutes(5, 30)
//    val currentTime = OffsetDateTime.now(istOffset)
//
//    // Get current day and time in the required format
//    val currentDay = currentTime.format(DateTimeFormatter.ofPattern("EEE")).toLowerCase
//    val currentTimeStr = currentTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
//
//    // MongoDB query
//    val query = or(
//      model.Filters.and(equal("days", currentDay), lte("from", currentTimeStr), gte("to", currentTimeStr)),
//      model.Filters.and(
//        notEqual("days", currentDay),
//        lte("from", currentTimeStr),
//        gte("from", "12:00 AM"),
//        gte("to", currentTimeStr)
//      ),
//      model.Filters.and(notEqual("days", currentDay), lte("from", currentTimeStr), lte("to", "12:00 AM"))
//    )
//
//    // Execute the query and check if a result is found
//    val result = collection
//      .find(query)
//      .projection(include("_id"))
//      .limit(1)
//      .toFuture()
//
//    val documents = Await.result(result, Duration.Inf)
//    documents.nonEmpty
//  }
}

