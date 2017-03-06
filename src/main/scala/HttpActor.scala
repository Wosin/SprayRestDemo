import JsonProtocol._
import akka.actor.{Actor, ActorLogging, ActorRefFactory}
import com.mongodb.client.model.Updates
import org.mongodb.scala.model.Filters
import org.mongodb.scala.{Document, MongoClient, MongoDatabase}
import spray.http._
import spray.httpx.SprayJsonSupport._
import spray.httpx.marshalling.ToResponseMarshallable
import spray.routing.HttpService

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

/**
  * Created by admin on 2/25/2017.
  */
class HttpActor extends Actor with HttpService with ActorLogging{
  def actorRefFactory: ActorRefFactory = context
  
  override def receive: Receive = runRoute(simpleRoute)
  val simpleRoute =
    pathPrefix("user") {
      path("add") {
        post {
          entity(as[Person]) { person =>
            var response:HttpResponse = HttpResponse(StatusCodes.InternalServerError)
            respondWithMediaType(MediaTypes.`application/json`) {
              complete {
                val client: MongoClient = MongoClient("mongodb://localhost:27017")
                val database: MongoDatabase = client.getDatabase("persons")
                val future = database.getCollection("persons").insertOne(
                  Document("_id" -> getNextSequence(client), "name" -> person.firstName,
                    "surname" -> person.name)).toFuture() map ((doc) => HttpResponse(StatusCodes.OK))
                val result = Await.result(future, 2.seconds)
                client.close()
                result
              }
            }
          }
        }
      }~
      pathPrefix("delete"){
        path(IntNumber){ id =>
          get{
            respondWithMediaType(MediaTypes.`application/json`) {
              complete{
              val client: MongoClient = MongoClient("mongodb://localhost:27017")
              val database: MongoDatabase = client.getDatabase("persons")
              val future:Future[HttpResponse] = database.getCollection("persons").deleteOne(Filters.eq("_id", id))
              .toFuture().map(result => {
                  HttpResponse(StatusCodes.OK,result.head.getDeletedCount.toString)
                })
                val result = Await.result(future, 2.seconds)
                client.close()
                result
              }
            }
          }
        }
      }
    }~
  pathPrefix("users") {
    pathEnd{
      get {
        respondWithMediaType(MediaTypes.`application/json`) {
          complete {
            val client: MongoClient = MongoClient("mongodb://localhost:27017")
            val database: MongoDatabase = client.getDatabase("persons")
            var resultList: ListBuffer[String] = ListBuffer()
            var response: Option[ToResponseMarshallable] = Option.empty
            val future: Future[HttpResponse] = database.getCollection("persons").find().toFuture() map { (docs) =>
              docs.foreach(doc => resultList += doc.toJson())
              HttpResponse(StatusCodes.OK, resultList.toList.toString())
            }
            val result = Await.result(future, 2.seconds)
            client.close()
            result
          }
        }
      }
    }~
    path(IntNumber) { id =>
      post {
        entity(as[Person]) { person =>
          respondWithMediaType(MediaTypes.`application/json`) {
            complete {
              val client: MongoClient = MongoClient("mongodb://localhost:27017")
              val database: MongoDatabase = client.getDatabase("persons")
              print("ID:" + id  )
              val future: Future[HttpResponse] = database.getCollection("persons").findOneAndReplace(
                Filters.eq("_id", id), Document("name" -> person.firstName, "surname" -> person.name,
                  "age" -> person.age)).toFuture().map(documents =>
                if (documents.isEmpty) {
                  HttpResponse(StatusCodes.OK, "No user with given Id.")
                } else {
                  HttpResponse(StatusCodes.OK, documents.head.toJson())
                }
              )
              val result = Await.result(future, 2.seconds)
              client.close()
              result
            }
          }
        }
      }~
      get {
        respondWithMediaType(MediaTypes.`application/json`) {
          complete {
            val client: MongoClient = MongoClient("mongodb://localhost:27017")
            val database: MongoDatabase = client.getDatabase("persons")
            var response: Option[ToResponseMarshallable] = Option.empty
            val future: Future[HttpResponse] = database.getCollection("persons").find(Filters.eq("_id", id))
            .toFuture() map { doc => {
              if (doc.isEmpty) {
                HttpResponse(StatusCodes.OK, "No User with given id")
              } else {
                HttpResponse(StatusCodes.OK, doc.head.toJson())
              }
            }
            }
            val result = Await.result(future, 2.seconds)
            client.close()
            result
          }
        }
      }
    }
  }
  
  def getNextSequence(client:MongoClient): Int = {
    var currentId = 0
    val db: MongoDatabase = client.getDatabase("persons")
  
    val future = db.getCollection("counters").findOneAndUpdate(
      Filters.eq("_id", "userid"),
      Updates.inc("seq", 1)).toFuture()
    
    Await.result(future, 1.seconds).head.get("seq").get.asInt32().getValue
  }
}


