package net.lambdaserve.mapextract

import munit.FunSuite

class MapExtractTest extends FunSuite:
  test("MapExtract should extract values from maps"):
    val map = Map("a" -> IndexedSeq("1"), "b" -> IndexedSeq("2"))
    assertEquals(MapExtract.extractString(Seq(map), "a", 0), "1")
    assertEquals(MapExtract.extractString(Seq(map), "b", 0), "2")

  test("MapExtract should throw exception when field not found"):
    val map = Map("a" -> IndexedSeq("1"), "b" -> IndexedSeq("2"))
    intercept[IllegalArgumentException] {
      MapExtract.extractString(Seq(map), "c", 0)
    }

  test("MapExtract should throw exception when index out of bounds"):
    val map = Map("a" -> IndexedSeq("1"), "b" -> IndexedSeq("2"))
    intercept[IllegalArgumentException] {
      MapExtract.extractString(Seq(map), "a", 1)
    }

  test(
    "MapExtract should extract values from the first map with the given prefix"
  ):
    val map1 = Map("a" -> IndexedSeq("1"), "b" -> IndexedSeq("2"))
    val map2 = Map("a" -> IndexedSeq("3"), "b" -> IndexedSeq("4"))

    assertEquals(MapExtract.extractString(Seq(map1, map2), "a", 0), "1")

  test("Complex example"):
    enum Months derives MapExtract:
      case Jan
      case Feb
      case Mar
      case Apr
      case May
      case Jun
      case Jul
      case Aug
      case Sep
      case Oct
      case Nov
      case Dec

    case class Pet(name: String, kind: String) derives MapExtract

    case class MyForm(
      firstName: String,
      lastName: String,
      points: List[Int],
      bornIn: Months,
      pet: List[Pet]
    ) derives MapExtract

    val me = summon[MapExtract[MyForm]]

    val example = Map(
      "firstName" -> IndexedSeq("Pet"),
      "lastName"  -> IndexedSeq("Owner"),
      "points"    -> IndexedSeq("12", "44", "52"),
      "bornIn"    -> IndexedSeq("Oct"),
      "pet.name"  -> IndexedSeq("Max", "Milly"),
      "pet.kind"  -> IndexedSeq("Dog", "Cat")
    )

    val mapped = me.projectMap(example)

    assert(mapped.firstName == "Pet")
    assert(mapped.lastName == "Owner")
    assert(mapped.points == List(12, 44, 52))
    assert(mapped.bornIn == Months.Oct)
    assert(mapped.pet == List(Pet("Max", "Dog"), Pet("Milly", "Cat")))

  test("MapExtract should handle HTTP GET query parameters"):
    case class SearchQuery(
      query: String,
      page: Int,
      limit: Int
    ) derives MapExtract

    val params = Map(
      "query" -> IndexedSeq("scala"),
      "page" -> IndexedSeq("1"),
      "limit" -> IndexedSeq("10")
    )

    val result: SearchQuery = summon[MapExtract[SearchQuery]].projectMap(params)

    assert(result.query == "scala")
    assert(result.page == 1)
    assert(result.limit == 10)

  test("MapExtract should handle form submission with nested objects"):
    case class Address(street: String, city: String, zip: String) derives MapExtract
    case class ContactForm(
      name: String,
      email: String,
      address: Address
    ) derives MapExtract

    val formData = Map(
      "name" -> IndexedSeq("John Doe"),
      "email" -> IndexedSeq("john@example.com"),
      "address.street" -> IndexedSeq("123 Main St"),
      "address.city" -> IndexedSeq("Springfield"),
      "address.zip" -> IndexedSeq("12345")
    )

    val form: ContactForm = summon[MapExtract[ContactForm]].projectMap(formData)

    assert(form.name == "John Doe")
    assert(form.email == "john@example.com")
    assert(form.address.street == "123 Main St")
    assert(form.address.city == "Springfield")
    assert(form.address.zip == "12345")

  test("MapExtract should handle optional fields with Some value"):
    case class SearchWithFilter(
      term: String,
      filter: Option[String]
    ) derives MapExtract

    val params = Map(
      "term" -> IndexedSeq("scala"),
      "filter" -> IndexedSeq("recent")
    )

    val result: SearchWithFilter = summon[MapExtract[SearchWithFilter]].projectMap(params)

    assert(result.term == "scala")
    assert(result.filter == Some("recent"))

  test("MapExtract should handle optional fields with None value"):
    case class SearchWithFilter(
      term: String,
      filter: Option[String]
    ) derives MapExtract

    val params = Map(
      "term" -> IndexedSeq("scala")
    )

    val result: SearchWithFilter = summon[MapExtract[SearchWithFilter]].projectMap(params)

    assert(result.term == "scala")
    assert(result.filter == None)

  test("MapExtract should handle multiple numeric types"):
    case class NumericForm(
      shortVal: Short,
      intVal: Int,
      longVal: Long,
      floatVal: Float,
      doubleVal: Double,
      bigDecimalVal: BigDecimal
    ) derives MapExtract

    val params = Map(
      "shortVal" -> IndexedSeq("100"),
      "intVal" -> IndexedSeq("1000"),
      "longVal" -> IndexedSeq("100000"),
      "floatVal" -> IndexedSeq("3.14"),
      "doubleVal" -> IndexedSeq("2.718281828"),
      "bigDecimalVal" -> IndexedSeq("999.999")
    )

    val result: NumericForm = summon[MapExtract[NumericForm]].projectMap(params)

    assert(result.shortVal == 100.toShort)
    assert(result.intVal == 1000)
    assert(result.longVal == 100000L)
    assert(result.floatVal == 3.14f)
    assert(result.doubleVal == 2.718281828)
    assert(result.bigDecimalVal == BigDecimal("999.999"))

  test("MapExtract should handle boolean values"):
    case class Preferences(
      newsletter: Boolean,
      notifications: Boolean
    ) derives MapExtract

    val params = Map(
      "newsletter" -> IndexedSeq("true"),
      "notifications" -> IndexedSeq("false")
    )

    val result: Preferences = summon[MapExtract[Preferences]].projectMap(params)

    assert(result.newsletter == true)
    assert(result.notifications == false)

  test("MapExtract should handle UUID fields"):
    import java.util.UUID

    case class Entity(id: UUID, name: String) derives MapExtract

    val uuid = UUID.randomUUID()
    val params = Map(
      "id" -> IndexedSeq(uuid.toString),
      "name" -> IndexedSeq("test-entity")
    )

    val result = summon[MapExtract[Entity]].projectMap(params)

    assert(result.id == uuid)
    assert(result.name == "test-entity")

  test("MapExtract should handle Vector collections"):
    case class VectorForm(tags: Vector[String]) derives MapExtract

    val params = Map(
      "tags" -> IndexedSeq("scala", "http", "server")
    )

    val result: VectorForm = summon[MapExtract[VectorForm]].projectMap(params)

    assert(result.tags == Vector("scala", "http", "server"))

  test("MapExtract should handle Seq collections"):
    case class SeqForm(items: Seq[Int]) derives MapExtract

    val params = Map(
      "items" -> IndexedSeq("1", "2", "3", "4", "5")
    )

    val result: SeqForm = summon[MapExtract[SeqForm]].projectMap(params)

    assert(result.items == Seq(1, 2, 3, 4, 5))

  test("MapExtract should handle SourceName annotation"):
    case class UserForm(
      @SourceName("user_name") name: String,
      @SourceName("user_email") email: String
    ) derives MapExtract

    val params = Map(
      "user_name" -> IndexedSeq("Jane"),
      "user_email" -> IndexedSeq("jane@example.com")
    )

    val result: UserForm = summon[MapExtract[UserForm]].projectMap(params)

    assert(result.name == "Jane")
    assert(result.email == "jane@example.com")

  test("MapExtract should handle deeply nested structures"):
    case class Coordinates(lat: Double, lon: Double) derives MapExtract
    case class Location(name: String, coords: Coordinates) derives MapExtract
    case class Event(title: String, location: Location) derives MapExtract

    val params = Map(
      "title" -> IndexedSeq("Conference"),
      "location.name" -> IndexedSeq("Convention Center"),
      "location.coords.lat" -> IndexedSeq("40.7128"),
      "location.coords.lon" -> IndexedSeq("-74.0060")
    )

    val result: Event = summon[MapExtract[Event]].projectMap(params)

    assert(result.title == "Conference")
    assert(result.location.name == "Convention Center")
    assert(result.location.coords.lat == 40.7128)
    assert(result.location.coords.lon == -74.0060)

  test("MapExtract should handle list of nested objects with empty lists"):
    case class Item(name: String) derives MapExtract
    case class EmptyList(items: List[Item]) derives MapExtract

    val params = Map(
      "items.name" -> IndexedSeq()
    )
  
    val result: EmptyList = summon[MapExtract[EmptyList]].projectMap(params)
    assert(result.items.isEmpty)
  
  test("MapExtract should handle mixed scalar and collection fields"):
    case class Survey(
      respondentId: String,
      age: Int,
      answers: List[String],
      completed: Boolean
    ) derives MapExtract

    val params = Map(
      "respondentId" -> IndexedSeq("R123"),
      "age" -> IndexedSeq("25"),
      "answers" -> IndexedSeq("yes", "no", "maybe"),
      "completed" -> IndexedSeq("true")
    )

    val result: Survey = summon[MapExtract[Survey]].projectMap(params)

    assert(result.respondentId == "R123")
    assert(result.age == 25)
    assert(result.answers == List("yes", "no", "maybe"))
    assert(result.completed == true)

  test("MapExtract should use first map when field exists in multiple maps"):
    case class Simple(value: String) derives MapExtract

    val map1 = Map("value" -> IndexedSeq("first"))
    val map2 = Map("value" -> IndexedSeq("second"))

    val result: Simple = summon[MapExtract[Simple]].projectMaps(Seq(map1, map2))

    assert(result.value == "first")

  test("MapExtract should fall back to second map when field not in first"):
    case class TwoFields(a: String, b: String) derives MapExtract

    val map1 = Map("a" -> IndexedSeq("valueA"))
    val map2 = Map("b" -> IndexedSeq("valueB"))

    val result: TwoFields = summon[MapExtract[TwoFields]].projectMaps(Seq(map1, map2))

    assert(result.a == "valueA")
    assert(result.b == "valueB")

  test("MapExtract should handle time types"):
    import java.time.{LocalDate, LocalDateTime, Instant, ZonedDateTime}
    import net.lambdaserve.mapextract.TimeInstances.given

    case class TimeForm(
      date: LocalDate,
      dateTime: LocalDateTime,
      instant: Instant,
      zonedDateTime: ZonedDateTime
    ) derives MapExtract

    val params = Map(
      "date" -> IndexedSeq("2023-12-25"),
      "dateTime" -> IndexedSeq("2023-12-25T10:15:30"),
      "instant" -> IndexedSeq("2023-12-25T10:15:30Z"),
      "zonedDateTime" -> IndexedSeq("2023-12-25T10:15:30+01:00[Europe/Paris]")
    )

    val result = summon[MapExtract[TimeForm]].projectMap(params)

    assert(result.date == LocalDate.parse("2023-12-25"))
    assert(result.dateTime == LocalDateTime.parse("2023-12-25T10:15:30"))
    assert(result.instant == Instant.parse("2023-12-25T10:15:30Z"))
    assert(result.zonedDateTime == ZonedDateTime.parse("2023-12-25T10:15:30+01:00[Europe/Paris]"))

  test("MapExtract should handle optional nested objects when present"):
    case class Address(city: String) derives MapExtract
    case class PersonWithOptionalAddress(
      name: String,
      address: Option[Address]
    ) derives MapExtract

    val params = Map(
      "name" -> IndexedSeq("John"),
      "address.city" -> IndexedSeq("NYC")
    )

    val result: PersonWithOptionalAddress = summon[MapExtract[PersonWithOptionalAddress]].projectMap(params)

    assert(result.name == "John")
    assert(result.address == Some(Address("NYC")))

  test("MapExtract should handle optional nested objects when absent"):
    case class Address(city: String) derives MapExtract
    case class PersonWithOptionalAddress(
      name: String,
      address: Option[Address]
    ) derives MapExtract

    val params = Map(
      "name" -> IndexedSeq("John")
    )

    val result: PersonWithOptionalAddress = summon[MapExtract[PersonWithOptionalAddress]].projectMap(params)

    assert(result.name == "John")
    assert(result.address == None)

  test("MapExtract should handle indexed collection notation"):
    case class Pet(name: String, age: Int) derives MapExtract
    case class PetOwner(
      ownerName: String,
      pet: List[Pet]
    ) derives MapExtract

    val params = Map(
      "ownerName" -> IndexedSeq("John"),
      "pet[0].name" -> IndexedSeq("Max"),
      "pet[0].age" -> IndexedSeq("3"),
      "pet[1].name" -> IndexedSeq("Minnie"),
      "pet[1].age" -> IndexedSeq("5")
    )

    val result: PetOwner = summon[MapExtract[PetOwner]].projectMap(params)

    assert(result.ownerName == "John")
    assert(result.pet.length == 2)
    assert(result.pet(0) == Pet("Max", 3))
    assert(result.pet(1) == Pet("Minnie", 5))

  test("MapExtract should handle indexed collection notation with gaps"):
    case class Item(value: String) derives MapExtract
    case class Container(items: List[Item]) derives MapExtract

    val params = Map(
      "items[0].value" -> IndexedSeq("first"),
      "items[2].value" -> IndexedSeq("third"),
      "items[5].value" -> IndexedSeq("sixth")
    )

    val result: Container = summon[MapExtract[Container]].projectMap(params)

    assert(result.items.length == 3)
    assert(result.items(0) == Item("first"))
    assert(result.items(1) == Item("third"))
    assert(result.items(2) == Item("sixth"))

  test("MapExtract should handle indexed nested objects in collections"):
    case class Address(street: String, city: String) derives MapExtract
    case class Person(name: String, addresses: List[Address]) derives MapExtract

    val params = Map(
      "name" -> IndexedSeq("Alice"),
      "addresses[0].street" -> IndexedSeq("123 Main St"),
      "addresses[0].city" -> IndexedSeq("Springfield"),
      "addresses[1].street" -> IndexedSeq("456 Oak Ave"),
      "addresses[1].city" -> IndexedSeq("Portland")
    )

    val result: Person = summon[MapExtract[Person]].projectMap(params)

    assert(result.name == "Alice")
    assert(result.addresses.length == 2)
    assert(result.addresses(0) == Address("123 Main St", "Springfield"))
    assert(result.addresses(1) == Address("456 Oak Ave", "Portland"))

  test("MapExtract should handle indexed notation with simple scalar collections"):
    case class ScalarForm(values: List[Int]) derives MapExtract

    val params = Map(
      "values" -> IndexedSeq("1", "2", "3")
    )

    val result: ScalarForm = summon[MapExtract[ScalarForm]].projectMap(params)

    assert(result.values == List(1, 2, 3))

  test("MapExtract backward compatibility: old format still works"):
    case class Pet(name: String, kind: String) derives MapExtract
    case class Owner(pets: List[Pet]) derives MapExtract

    val params = Map(
      "pets.name" -> IndexedSeq("Max", "Milly"),
      "pets.kind" -> IndexedSeq("Dog", "Cat")
    )

    val result: Owner = summon[MapExtract[Owner]].projectMap(params)

    assert(result.pets.length == 2)
    assert(result.pets(0) == Pet("Max", "Dog"))
    assert(result.pets(1) == Pet("Milly", "Cat"))

end MapExtractTest
