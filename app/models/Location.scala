package models

import play.api.libs.json.{Json, OFormat}

case class Location(latitude:Double,longitude:Double)





  object Location {
    implicit val formatter: OFormat[Location] = Json.format[Location]
  }






