package persistence

import javax.inject.{Inject, Singleton}
import models.Person
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PersonRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {
  import profile.api._

  private val People = TableQuery[PeopleTable]

  def all(): Future[Seq[Person]] = db.run(People.result)

  def insert(person: Person): Future[Unit] = db.run(People += person).map{_ => ()}

  private class PeopleTable(tag: Tag) extends Table[Person](tag, "TBL_PEOPLE") {
    def document = column[String]("DOCUMENT", O.PrimaryKey)
    def name = column[String]("NAME")
    def lastName = column[String]("LAST_NAME")

    def * = (document, name, lastName) <> ((Person.apply _).tupled, Person.unapply)
  }
}