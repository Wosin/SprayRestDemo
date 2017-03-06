import akka.actor.{ActorSystem, Props}
import akka.util.Timeout
import spray.can.Http
import akka.io.IO
import akka.pattern.ask
import scala.concurrent.duration.DurationInt

object  Boot extends App{
  implicit  val system = ActorSystem("dominWos")
  val service = system.actorOf(Props[HttpActor])
  implicit val timeout = Timeout(5.seconds)
  IO(Http)  ? Http.Bind(service, interface = "localhost", port = 8080)
}
