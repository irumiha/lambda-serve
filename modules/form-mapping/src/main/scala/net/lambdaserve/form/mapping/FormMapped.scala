package net.lambdaserve.form.mapping

import magnolia1.*

trait FormMapped[T]:
  def mapForm(m: Map[String, IndexedSeq[String]], prefix: String = "", offset: Int = 0): T

object FormMapped extends Derivation[FormMapped] with BaseInstances with ContainerInstances:
  def extractString(
    m: Map[String, IndexedSeq[String]],
    prefix: String,
    offset: Int = 0
  ): String =
    val entries =
      m.getOrElse(prefix, throw IllegalArgumentException(s"Field not found at path $prefix"))
    if entries.length <= offset then
      throw new IllegalArgumentException(s"Index out of bounds at path $prefix, offset $offset")
    entries(offset)

  override def split[T](ctx: SealedTrait[FormMapped.Typeclass, T]): FormMapped.Typeclass[T] =
    new FormMapped[T]:
      override def mapForm(m: Map[String, IndexedSeq[String]], prefix: String, offset: Int): T =
        val stringValue = extractString(m, prefix)
        val subtype     = ctx.subtypes.find(_.typeInfo.short == stringValue).get

        subtype.typeclass.mapForm(m, prefix, offset)

  override def join[T](ctx: CaseClass[FormMapped.Typeclass, T]): FormMapped.Typeclass[T] =
    new FormMapped[T]:
      override def mapForm(m: Map[String, IndexedSeq[String]], prefix: String, offset: Int): T =
        val allParams = ctx.parameters.map { param =>
          val currentPrefix =
            if prefix.isEmpty then param.label
            else s"$prefix.${param.label}"
          param.typeclass.mapForm(m, currentPrefix, offset)
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

  object Months:
    given formMapper: FormMapped[Months] = FormMapped.derived

  case class Pet(name: String, breed: String)
  object Pet:
    given formMapper: FormMapped[Pet] = FormMapped.derived

  case class MyForm(
    firstName: String,
    lastName: String,
    points: List[Int],
    bornIn: Months,
    pet: List[Pet]
  )
  object MyForm:
    given formMapper: FormMapped[MyForm] = FormMapped.derived

  def printForm[F](formData: Map[String, IndexedSeq[String]])(using f: FormMapped[MyForm]): Unit =
    val mapped = f.mapForm(formData)
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
