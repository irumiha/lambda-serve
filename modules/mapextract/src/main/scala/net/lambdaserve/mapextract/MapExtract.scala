package net.lambdaserve.mapextract

import magnolia1.*

trait MapExtract[T]:
  def projectMap(
    m: Map[String, IndexedSeq[String]],
    prefix: String = "",
    offset: Int = 0
  ): T = projectMaps(Seq(m), prefix, offset)

  def projectMaps(
    ms: Seq[Map[String, IndexedSeq[String]]],
    prefix: String = "",
    offset: Int = 0
  ): T

object MapExtract
    extends AutoDerivation[MapExtract]
    with BaseInstances
    with ContainerInstances:
  def extractString(
    ms: Seq[Map[String, IndexedSeq[String]]],
    prefix: String,
    offset: Int = 0
  ): String =
    val entries =
      ms.find(_.contains(prefix))
        .map(_.apply(prefix))
        .getOrElse(
          throw IllegalArgumentException(s"Field not found at path $prefix")
        )
    if entries.length <= offset then
      throw new IllegalArgumentException(
        s"Index out of bounds at path $prefix, offset $offset"
      )
    entries(offset)

  override def split[T](
    ctx: SealedTrait[MapExtract.Typeclass, T]
  ): MapExtract.Typeclass[T] =
    new MapExtract[T]:
      override def projectMaps(
        ms: Seq[Map[String, IndexedSeq[String]]],
        prefix: String,
        offset: Int
      ): T =
        val stringValue = extractString(ms, prefix)
        val subtype     = ctx.subtypes.find(it => it.typeInfo.short == stringValue).get

        subtype.typeclass.projectMaps(ms, prefix, offset)

  override def join[T](
    ctx: CaseClass[MapExtract.Typeclass, T]
  ): MapExtract.Typeclass[T] =
    new MapExtract[T]:
      override def projectMaps(
        ms: Seq[Map[String, IndexedSeq[String]]],
        prefix: String,
        offset: Int
      ): T =
        val allParams = ctx.parameters.map { param =>
          val currentPrefix =
            if prefix.isEmpty then param.label
            else s"$prefix.${param.label}"
          param.typeclass.projectMaps(ms, currentPrefix, offset)
        }

        ctx.rawConstruct(allParams)

@main
def main(args: String*): Unit =
  enum Months:
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

  case class Pet(name: String, breed: String)

  case class MyForm(
    firstName: String,
    lastName: String,
    points: List[Int],
    bornIn: Months,
    pet: List[Pet]
  ) derives MapExtract

  def printForm[F](formData: Map[String, IndexedSeq[String]])(using
    f: MapExtract[MyForm]
  ): Unit =
    val mapped = f.projectMap(formData)
    println(mapped)

  printForm(
    Map(
      "firstName" -> IndexedSeq("Pero"),
      "lastName"  -> IndexedSeq("Milikona"),
      "points"    -> IndexedSeq("12", "44", "52"),
      "bornIn"    -> IndexedSeq("Oct"),
      "pet.name"  -> IndexedSeq("Max", "Milly"),
      "pet.breed" -> IndexedSeq("Dog", "Cat")
    )
  )
