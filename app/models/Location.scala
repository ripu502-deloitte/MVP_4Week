package models

import play.api.libs.json.{Json, OFormat}

case class Location(longitude:Double,latitude:Double)





  object Location {
    implicit val formatter: OFormat[Location] = Json.format[Location]
  }






