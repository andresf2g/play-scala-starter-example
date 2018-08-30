package controllers

import javax.inject._
import play.api.mvc._
import play.api.Configuration
import play.api.Logger
import play.api.libs.json._
import java.nio.file.{Files => JFiles, Path, Paths}
import play.api.mvc.RequestHeader

@Singleton
class HelloController @Inject() (config: Configuration, cc: ControllerComponents) extends AbstractController(cc) {
  Logger.info(config.get[String]("application.name"))
  def hello = Action {
    Ok(<h1>Hello World!</h1>).withHeaders(CACHE_CONTROL -> "max-age=3600", ETAG -> "xx").as(HTML)
  }

  def error(message: Option[String]) = Action { InternalServerError(s"Oops! Internal Server Error $message") }

  def bad = Action { Unauthorized("Unauthorized!") }

  def pdf = Action { Redirect("/assets/pdf-file.pdf") }

  def postData = Action { request=>
    Logger.info("Headers got from request" + request.headers.get("My-Header"))
    Logger.info("Cookies got from request" + request.cookies.get("My-Cookie"))
    /*Ok("Post data").withCookies(Cookie("theme", "blue")).bakeCookies()*/
    Ok("Post data").withCookies(Cookie("theme", "blue")).bakeCookies()
  }

  def putData = Action { Ok("Put data") }

  def deleteData(id: String) = Action { Ok(s"Deleted element: $id") }

  def patchData = Action {
    /*Ok(JsObject(Seq("name" -> JsString("Watership Down"), "lastname" -> JsString("Lastname"))))*/

    implicit val locationWrites = new Writes[Location] {
      def writes(location: Location) = Json.obj(
        "lat" -> location.lat,
        "long" -> location.long
      )
    }

    implicit val residentWrites = new Writes[Resident] {
      def writes(resident: Resident) = Json.obj(
        "name" -> resident.name,
        "age" -> resident.age,
        "role" -> resident.role
      )
    }

    implicit val placeWrites = new Writes[Place] {
      def writes(place: Place) = Json.obj(
        "name" -> place.name,
        "location" -> place.location,
        "residents" -> place.residents
      )
    }

    val json = Json.toJson(Place(
      "Watership Down",
      Location(51.235685, -1.309197),
      Seq(
        Resident("Fiver", 4, None),
        Resident("Bigwig", 6, Some("Owsla"))
      )
    ))

    println(json("name"))
    println(json("residents")(1))

    Ok(json)
  }

  def upload = Action(parse.multipartFormData) { implicit request =>
    request.body.file("picture").map { picture =>
      val filename = Paths.get(picture.filename).getFileName
      picture.ref.moveTo(Paths.get(s"C:/Users/andres.giraldo/Downloads/$filename"), replace = true)
      Ok("File uploaded")
    }.getOrElse {
      Redirect(routes.HomeController.index).flashing("error" -> "Missing name")
    }
  }

  case class Location(lat: Double, long: Double)
  case class Resident(name: String, age: Int, role: Option[String])
  case class Place(name: String, location: Location, residents: Seq[Resident])


}