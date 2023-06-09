package models

import play.api.libs.json.{Json, OFormat}

case class Res(distance:Double,duration:Double)
object Res {
  implicit val formatter: OFormat[Res] = Json.format[Res]
}