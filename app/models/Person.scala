package models

import play.api.libs.json.Json

case class Person(document: String, name: String, lastName: String)

object Person {
  var personList: List[Person] = {
    List(
      Person("111", "Andres", "Giraldo"),
      Person("112", "Sulany", "Ceballos")
    )
  }

  def save(person: Person)= {
    personList = personList ::: List(person)
  }
}
