package controllers

import models.{Location, Res, Restaurant}
import org.mongodb.scala.{Document, _}
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json._
import play.api.libs.ws.WSClient
import play.api.mvc._
import scalaj.http.{Http, HttpResponse}
import services.RestaurantService

import java.time.{DayOfWeek, LocalDate, LocalTime}
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}


class RestaurantController @Inject()(rs:RestaurantService,cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  val mongoClient: MongoClient = MongoClient()
  val database: MongoDatabase = mongoClient.getDatabase("test")
  val collection: MongoCollection[Document] = database.getCollection("Restaurants")


  implicit val restaurantWrites: Writes[Restaurant] = (
    (JsPath \ "_id").write[String] and
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
    rs.searchByState(state)
      .map(restaurants => Ok(Json.toJson(restaurants)))
      .recover {
        case ex: Exception => InternalServerError(s"An error occurred: ${ex.getMessage}")
      }
  }

  def searchByCuisine(longitude: Double, latitude: Double,cuisine: String): Action[AnyContent] = Action.async { implicit request =>

    rs.searchByCuisine(longitude, latitude, cuisine)
      .map(restaurants => Ok(Json.toJson(restaurants)))
      .recover {
        case ex: Exception => InternalServerError(s"An error occurred: ${ex.getMessage}")
      }
  }



  def searchRestaurantsNearby(longitude: Double, latitude: Double): Action[AnyContent]
  = Action.async { implicit request: Request[AnyContent] =>

    rs.searchRestaurantsNearby(longitude, latitude)
      .map(restaurants => Ok(Json.toJson(restaurants)))
      .recover{ case ex: Exception => InternalServerError(s"An error occurred: ${ex.getMessage}") }
  }

  private def getRestaurant(document: Document) = {
    Restaurant(
      document.getString("_id"),
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

  def calculateDistance(userLat: Double, userLon: Double,restId:String)= Action { implicit request: Request[AnyContent] =>
      val json = rs.calculateDistance(userLat,userLon, restId)
      val distance = (json \ "distances")(0)(1).as[Double]
      val duration = (json \ "durations")(0)(1).as[Double]
      val k=distance
      val v=duration

      println(s"Distance: $distance meters")
      println(s"Duration: $duration seconds")


    Ok(Json.toJson(Res(k,v)))

  }




  def CheckIfOpen(restId:String)= Action { implicit request: Request[AnyContent] =>
    val check=rs.CheckIfOpen(restId)
     if(check) println("open")
     else println("closed")
    val result = if (check) "Restaurant is Open" else "Restaurant is Closed"
    Ok(result )
  }

  def checkChainRestaurants(longitude: Double, latitude: Double ): Action[AnyContent]
  = Action.async { implicit request=>
      rs.checkChainRestaurants(longitude, latitude).map(restaurants => Ok(Json.toJson(restaurants)))
        .recover {
          case ex: Exception => InternalServerError(s"An error occurred: ${ex.getMessage}")
        }
  }

  def SearchRestaurants(longitude: Double, latitude: Double ,RestName:String): Action[AnyContent]
  = Action.async { implicit request: Request[AnyContent] =>

    rs.SearchRestaurants(longitude, latitude, RestName)
      .map(restaurants => Ok(Json.toJson(restaurants)))
      .recover {
        case ex: Exception => InternalServerError(s"An error occurred: ${ex.getMessage}")
      }

  }




}

