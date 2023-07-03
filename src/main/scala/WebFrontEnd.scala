```scala
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

object WebFrontEnd {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("web-frontend")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val config = ConfigFactory.load()
    val serverPort = config.getInt("app.server.port")

    val route =
      path("") {
        getFromResource("public/index.html")
      } ~
        pathPrefix("css") {
          getFromResourceDirectory("public/css")
        } ~
        pathPrefix("js") {
          getFromResourceDirectory("public/js")
        }

    Http().bindAndHandle(route, "localhost", serverPort)
  }
}
```