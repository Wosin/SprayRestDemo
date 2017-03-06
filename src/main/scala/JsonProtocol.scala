import org.mongodb.scala.Document
import spray.json.{DefaultJsonProtocol, JsObject, JsValue, RootJsonFormat}

/**
  * Created by admin on 2/25/2017.
  */
object JsonProtocol extends DefaultJsonProtocol {
  
  implicit val PersonFormat = jsonFormat3(Person)
 
}
case class Person(name: String, firstName: String, age: Long)
