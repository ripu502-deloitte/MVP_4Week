package Models

import play.api.libs.json.Json
import reactivemongo.api.bson.{BSONDocumentReader, BSONDocumentWriter, BSONObjectID, Macros}

case class Restaurants(restaurantName: String,
                       cuisine: String,
                       openHours: String,
                       state: String,
                       cntyGeoid: String,
                       cntyName: String,
                       uaGeoid: String,
                       uaName: String,
                       msaGeoid: String,
                       msaName: String,
                       lon: String,
                       lat: String,
                       frequency: String,
                       isChain: String
                     )

object Restaurants {
  implicit val format = Json.format[Restaurants]
  implicit val reader: BSONDocumentReader[Restaurants] = Macros.reader[Restaurants]
  implicit val writer: BSONDocumentWriter[Restaurants] = Macros.writer[Restaurants]
}


