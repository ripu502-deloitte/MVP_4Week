//package services
//
//import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
//import models.{Location, Restaurant}
//import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}
//import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
//import play.api.libs.json.{JsPath, Json, Writes}
//import play.api.mvc.Results.InternalServerError
//import play.api.mvc.{Action, AnyContent}
//
//import javax.inject.Inject
//import scala.concurrent.{ExecutionContext, Future}
//
//
//class RestaurantService @Inject()(collection: MongoCollection[Document])(implicit ec: ExecutionContext){
//
//  //  val mongoClient: MongoClient = MongoClient()
//  //  val database: MongoDatabase = mongoClient.getDatabase("test")
//  //  val collection: MongoCollection[Document] = database.getCollection("Restaurants")
//
//
//
//  private def getRestaurant(document: Document) = {
//    Restaurant(
//      document.getString("_id"),
//      document.getString("restaurantName"),
//      document.getString("cuisine"),
//      document.getString("openHours"),
//      document.getString("state"),
//      document.getString("cntyGeoid"),
//      document.getString("cntyName"),
//      document.getString("uaGeoid"),
//      document.getString("uaName"),
//      document.getString("msaGeoid"),
//      document.getString("msaName"),
//      document.getString("lon"),
//      document.getString("lat"),
//      document.getString("frequency"),
//      document.getString("isChain"),
//      Location(document.getString("lat").toDouble, document.getString("lon").toDouble)
//    )
//  }
//
//  def searchByState(state: String): Future[List[Restaurant]] = {
//    val query = Document("state" -> state.toUpperCase())
//    collection.find(query).limit(5).toFuture()
//      .map(
//        documents => documents.map(document => getRestaurant(document)).toList
//
//      )
//
//  }
//
//}
