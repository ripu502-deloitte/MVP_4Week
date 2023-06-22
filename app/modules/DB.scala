package modules

import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}

 object DB {
  val mongoClient: MongoClient = MongoClient()
  val database: MongoDatabase = mongoClient.getDatabase("test")
  val collection: MongoCollection[Document] = database.getCollection("Restaurants")

}
