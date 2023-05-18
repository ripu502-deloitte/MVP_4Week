package modules

import Models.Restaurant
import Repositories.RestaurantRepository

import javax.inject.{Inject, Singleton}
import scala.io.Source

@Singleton
class Ingest @Inject()(restaurantRepository: RestaurantRepository) {
  println("hello")
  val csvFile = "/home/svinayakamnigam/Downloads/merge-csv.com__64647fa425e55.csv"
  val source = Source.fromFile(csvFile)
  val lines = source.getLines().toList
  for (k <- lines.drop(1)) { //drop headers
    var values: Array[String] = k.split(",") //regex for getting commas only which do not come inside a number and are not betwwen spaces
    val restaurant = Restaurant(
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
    restaurantRepository.create(restaurant)
  }

}
