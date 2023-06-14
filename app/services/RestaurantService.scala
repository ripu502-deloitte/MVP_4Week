package services

import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import models.{Location, Res, Restaurant}
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, JsValue, Json, Writes}
import play.api.mvc.Results.InternalServerError
import play.api.mvc.{Action, AnyContent, Request}
import scalaj.http.{Http, HttpResponse}

import java.time.format.DateTimeFormatter
import java.time.{DayOfWeek, LocalDate, LocalTime}
import javax.inject.Inject
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, ExecutionContext, Future}


class RestaurantService @Inject()(implicit ec: ExecutionContext) {

  val mongoClient: MongoClient = MongoClient()
  val database: MongoDatabase = mongoClient.getDatabase("test")
  val collection: MongoCollection[Document] = database.getCollection("Restaurants")


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

  def searchByState(state: String): Future[List[Restaurant]] = {
    val query = Document("state" -> state.toUpperCase())
    collection.find(query).limit(5).toFuture()
      .map(
        documents => documents.map(document => getRestaurant(document)).toList

      )

  }

  def CheckIfOpen(restId: String): Boolean = {
    val query = Document("_id" -> restId)
    val rest = collection.find(query).toFuture()
    val extractedValue = Await.result(rest, 5.seconds)
    val doc = extractedValue.head
    val openHoursString = doc.getString("openHours")
    val allSlots = openHoursString.split("\\|").map(_.trim)
    val currentDayOfWeek = LocalDate.now().getDayOfWeek.toString
    val day = currentDayOfWeek.substring(0, 3)
    val currentTime = LocalTime.now()
    var check: Boolean = false
    for (oneslot <- allSlots) {

      val daysandtime = oneslot.split("\\s+")
      daysandtime.foreach(print)
      var len = daysandtime.length
      println("length", len)
      len.toInt match {
        case 5 => {
          var openday = daysandtime(0).toString()
          println("day", openday)
          var startTime = daysandtime(1).toString.concat(daysandtime(2).toString)
          println("start", startTime)
          var endTime = daysandtime(3).toString.concat(daysandtime(4).toString)
          println("end", endTime)
          val formatter = DateTimeFormatter.ofPattern("hmma")
          val startTimefinal = LocalTime.parse(startTime, formatter)
          val endTimefinal = LocalTime.parse(endTime, formatter)
          println("stf", startTimefinal)
          println("etf", endTimefinal)
          println("ct", currentTime)
          if (openday.equalsIgnoreCase(day) && (currentTime.isAfter(startTimefinal) && currentTime.isBefore(endTimefinal))) {
            check = true
          }


        }
        case 6 => {
          var startTime = daysandtime(2).toString.concat(daysandtime(3).toString)
          var endTime = daysandtime(4).toString.concat(daysandtime(5).toString)
          println("start", startTime)
          println("end", endTime)
          val formatter = DateTimeFormatter.ofPattern("hmma")
          val startTimefinal = LocalTime.parse(startTime, formatter)
          val endTimefinal = LocalTime.parse(endTime, formatter)

          val daysOfWeek = DayOfWeek.values().toList
          val list = ListBuffer[String]()
          for (k <- daysOfWeek) {
            list += k.toString.substring(0, 3)
          }
          var startDay = daysandtime(0).toString().toUpperCase()
          var endDay = daysandtime(1).toString().toUpperCase()
          var today = day.toUpperCase()
          val si = list.indexOf(startDay)
          val ei = list.indexOf(endDay)
          val ci = list.indexOf(today)
          if (si < ei) {
            if (ci >= si && ci <= ei && (currentTime.isAfter(startTimefinal) && currentTime.isBefore(endTimefinal))) {
              check = true
            }
          }
          else if (si > ei) {
            if (ci <= si || ci >= ei && (currentTime.isAfter(startTimefinal) && currentTime.isBefore(endTimefinal))) {
              check = true
            }
          }


        }

        case _ => {
          println("in _")
          check = false
        }
      }

    }
    return check
  }


  def searchByCuisine(cuisine: String): Future[List[Restaurant]] = {
    val query = Document("cuisine" -> cuisine.toLowerCase().capitalize)
    collection.find(query).limit(5).toFuture()
      .map(
        documents => documents.map(document => getRestaurant(document)).toList

      )
  }

  def SearchRestaurants(longitude: Double, latitude: Double ,RestName:String): Future[List[Restaurant]] = {

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

    collection.find(query).toFuture().map(

      documents => documents.map(document => getRestaurant(document)).toList.filter(x=> x.restaurantName.equalsIgnoreCase(RestName))
      )

  }

  def calculateDistance(userLat: Double, userLon: Double,restId:String): JsValue = {
    val query = Document("_id" -> restId)
    val rest = collection.find(query).toFuture()
    val extractedValue = Await.result(rest, 5.seconds)
    val doc=extractedValue.head
    val restLon = doc.getString("lon").toDouble
    val restLat = doc.getString("lat").toDouble
    var k,v=0.0
    //    println(fieldValue,otherFieldValue)

    val directionsUrl = s"https://api.mapbox.com/directions-matrix/v1/mapbox/driving/$userLon,$userLat;$restLon,$restLat?sources=0&annotations=distance,duration&approaches = curb;curb&access_token=pk.eyJ1IjoiYWFuY2hhbDAxIiwiYSI6ImNsaWlpNHFsZjAwY28zZG1menU4c29jbzAifQ.IBJUQCFFAOs6BM1bPrEk6Q"


    val response: HttpResponse[String] = Http(directionsUrl).asString
    Json.parse(response.body)



  }
}
