package controllers

import Models.{ Restaurants}
import Repositories.RestaurantRepository
import play.api.libs.json.{JsValue, Json}

import javax.inject.Inject
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source


class RestaurantController @Inject()(cc: ControllerComponents, restaurantRepository: RestaurantRepository)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def createUser = Action.async(parse.json) { request =>
    request.body.validate[Restaurants].map { restaurant =>
      restaurantRepository.create(restaurant).map(_ => Ok("User created"))
    }.getOrElse(Future.successful(BadRequest("Invalid user format")))
  }

  def getUsers = Action.async { _ =>
    restaurantRepository.findAll().map(users => Ok(Json.toJson(users)))
  }

//  def ingest(): Unit=
//  {
//    val csvFile = "/home/svinayakamnigam/Downloads/merge-csv.com__64647fa425e55.csv"
//    val source = Source.fromFile(csvFile)
//    val lines = source.getLines().toList
//    for (k <- lines.drop(1)) { //drop headers
//      var values: Array[String] = k.split(",") //regex for getting commas only which do not come inside a number and are not betwwen spaces
//      val restaurant = Restaurant(
//        values(0),
//        values(1),
//        values(2),
//        values(3),
//        values(4),
//        values(5),
//        values(6),
//        values(7),
//        values(8),
//        values(9),
//        values(10),
//        values(11),
//        values(12),
//        values(13)
//      )
//      restaurantRepository.create(restaurant)
//    }
//  }

}
