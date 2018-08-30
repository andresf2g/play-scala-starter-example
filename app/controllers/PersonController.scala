package controllers

import javax.inject._
import models.Person
import persistence.PersonRepository
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PersonController @Inject() (cc: ControllerComponents, personRepository: PersonRepository) (implicit ec: ExecutionContext) extends AbstractController(cc){
  populateData()

  implicit val personWrites: Writes[Person] = (
    (JsPath \ "document").write[String] and
      (JsPath \ "name").write[String] and
      (JsPath \ "lastName").write[String]
  )(unlift(Person.unapply))

  implicit  val personReads: Reads[Person] = (
    (JsPath \ "document").read[String] and
      (JsPath \ "name").read[String] and
      (JsPath \ "lastName").read[String]
  ) (Person.apply _)

  def listPeople = Action {
    Ok(Json.toJson(Person.personList))
  }

  def savePerson = Action(parse.json) { request =>
    val personResult = request.body.validate[Person]
    personResult.fold(
      errors => {
        BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toJson(errors)))
      },
      person => {
        Person.save(person)
        Ok(Json.obj("status" -> "OK", "message" -> ("Person '" + person.name + " " + person.lastName + "' saved.")))
      }
    )
  }

  def selectPeople = Action.async { implicit request: Request[AnyContent] =>
    val people = personRepository.all()
    people.map(s => Ok(s))
  }

  def createPerson = Action.async(parse.json[Person]) { request =>
    insertPerson(request.body)
  }

  private def insertPerson(person: Person): Future[Result] = {
    personRepository.insert(person)
      .map(_ => Ok(""))
      .recoverWith {
        case _: Exception => Future.successful( InternalServerError("No pudo guardarse el registro") )
      }
  }

  private def populateData() {
    insertPerson(new Person("111", "Andres", "Giraldo"))
    insertPerson(new Person("112", "Sulany", "Ceballos"))
    insertPerson(new Person("113", "Carolina", "Perez"))
  }
}
