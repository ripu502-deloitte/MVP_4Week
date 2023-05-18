package controllers
import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.io.Source
import play.api.mvc._
import play.api.libs.json._
import org.mongodb.scala.{Document, _}
import org.mongodb.scala.bson._
import models.Restaurant
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}

class RestaurantController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  val mongoClient: MongoClient = MongoClient()
  val database: MongoDatabase = mongoClient.getDatabase("mydatabase")
  val collection: MongoCollection[Document] = database.getCollection("restaurants")

  def ingestCSV(): Action[AnyContent] = Action.async { implicit request =>
    val csvFile = "/home/mayanksharma43/Downloads/chainness_point_2021_part3.csv"
    val source = Source.fromFile(csvFile)
    val lines = source.getLines().toList
    source.close()

    val header :: rows = lines
    val restaurants = rows.map(row => {
      val values = row.split(",").map(_.trim)
      Restaurant(
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
    })

    val documents = restaurants.map(restaurant => {
      Document(
        "restaurantName" -> restaurant.restaurantName,
        "cuisine" -> restaurant.cuisine,
        "openHours" -> restaurant.openHours,
        "state" -> restaurant.state,
        "cntyGeoid" -> restaurant.cntyGeoid,
        "cntyName" -> restaurant.cntyName,
        "uaGeoid" -> restaurant.uaGeoid,
        "uaName" -> restaurant.uaName,
        "msaGeoid" -> restaurant.msaGeoid,
        "msaName" -> restaurant.msaName,
        "lon" -> restaurant.lon,
        "lat" -> restaurant.lat,
        "frequency" -> restaurant.frequency,
        "isChain" -> restaurant.isChain
      )
    })

    collection.insertMany(documents).toFuture()
      .map(_ => Ok("CSV file ingested successfully"))
      .recover {
        case ex: Exception => InternalServerError(s"An error occurred: ${ex.getMessage}")
      }
  }


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
      (JsPath \ "isChain").write[String]
    )(unlift(Restaurant.unapply))


  implicit val restaurantSeqWrites: Writes[Seq[Restaurant]] = Writes.seq(restaurantWrites)

  def searchByState(state: String): Action[AnyContent] = Action.async { implicit request =>
    val query = Document("state" -> state)
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
            document.getString("isChain")
          )
        })
        Ok(Json.toJson(restaurants))
      })
      .recover {
        case ex: Exception => InternalServerError(s"An error occurred: ${ex.getMessage}")
      }
  }




}
