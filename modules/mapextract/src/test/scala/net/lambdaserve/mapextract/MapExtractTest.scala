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

end MapExtractTest
