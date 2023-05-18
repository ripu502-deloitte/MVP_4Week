package Repositories


import Models.{ Restaurants}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.{BSONDocument, BSONObjectID}
import reactivemongo.api.bson.collection.BSONCollection

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class RestaurantRepository @Inject()(reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) {

  def collection: Future[BSONCollection] =
    reactiveMongoApi.database.map(_.collection("Restaurants"))

  def create(restaurant: Restaurants): Future[Unit] =
    collection.flatMap(_.insert.one(restaurant).map(_ => ()))

  def findById(id: BSONObjectID): Future[Option[Restaurants]] =
    collection.flatMap(_.find(BSONDocument("_id" -> id)).one[Restaurants])

  def findByName(name: String): Future[Option[Restaurants]] =
    collection.flatMap(_.find(BSONDocument("name" -> name)).one[Restaurants])

  def findAll(): Future[List[Restaurants]] =
    collection.flatMap(_.find(BSONDocument.empty).cursor[Restaurants]().collect[List]())

//  def update(restaurant: Restaurant): Future[Unit] = {
//    val selector = BSONDocument("_id" -> restaurant._id)
//    val update = BSONDocument("$set" -> restaurant)
//    collection.flatMap(_.update.one(selector, update)).map(_ => ())
//  }

  def delete(id: BSONObjectID): Future[Unit] =
    collection.flatMap(_.delete.one(BSONDocument("_id" -> id)).map(_ => ()))


}