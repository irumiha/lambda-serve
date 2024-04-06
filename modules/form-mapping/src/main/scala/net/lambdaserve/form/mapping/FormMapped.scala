package net.lambdaserve.form.mapping

import magnolia1.*

trait FormMapped[T]:
  def mapForm(m: Map[String, Seq[String]], prefix: String = ""): T

object FormMapped extends Derivation[FormMapped]:
  private def extractString(m: Map[String, Seq[String]], prefix: String): String =
    m.getOrElse(prefix, throw IllegalArgumentException(s"Field not found at path $prefix"))
      .headOption
      .getOrElse(throw IllegalArgumentException(s"No value found at path $prefix"))

  given FormMapped[Int] with
    override def mapForm(m: Map[String, Seq[String]], prefix: String): Int =
      extractString(m, prefix).toInt

  given FormMapped[Float] with
    override def mapForm(m: Map[String, Seq[String]], prefix: String): Float =
      extractString(m, prefix).toFloat

  given FormMapped[Double] with
    override def mapForm(m: Map[String, Seq[String]], prefix: String): Double =
      extractString(m, prefix).toDouble

  given FormMapped[BigDecimal] with
    override def mapForm(m: Map[String, Seq[String]], prefix: String): BigDecimal =
      BigDecimal(extractString(m, prefix))

  given FormMapped[Boolean] with
    override def mapForm(m: Map[String, Seq[String]], prefix: String): Boolean =
      extractString(m, prefix).toBoolean

  given seq[T](using sfm: FormMapped[T]): FormMapped[Seq[T]] with
    override def mapForm(m: Map[String, Seq[String]], prefix: String): Seq[T] =
      m.getOrElse(prefix, throw IllegalArgumentException(s"Field not found at path $prefix"))
        .map { it =>
          sfm.mapForm(Map("" -> Seq(it)))  // TODO Super-inefficient
        }

  given list[T](using sfm: FormMapped[Seq[T]]): FormMapped[List[T]] with
    override def mapForm(m: Map[String, Seq[String]], prefix: String): List[T] =
      sfm.mapForm(m, prefix).toList

  given FormMapped[String] with
    override def mapForm(m: Map[String, Seq[String]], prefix: String): String =
      extractString(m, prefix)

  override def split[T](ctx: SealedTrait[FormMapped.Typeclass, T]): FormMapped.Typeclass[T] =
    new FormMapped[T]:
      override def mapForm(m: Map[String, Seq[String]], prefix: String): T =
        val stringValue = extractString(m, prefix)
        val subtype = ctx.subtypes.find(_.typeInfo.short == stringValue).get

        subtype.typeclass.mapForm(m, prefix)

  override def join[T](ctx: CaseClass[FormMapped.Typeclass, T]): FormMapped.Typeclass[T] =
    new FormMapped[T]:
      override def mapForm(m: Map[String, Seq[String]], prefix: String): T =
        val allParams = ctx.parameters.map { param =>
          val currentPrefix =
            if prefix.isEmpty then
              param.label
            else
              s"$prefix.${param.label}"
          param.typeclass.mapForm(m, currentPrefix)
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

  case class MyForm(firstName: String, lastName: String, points: List[Int], bornIn: Months)
  object MyForm:
    given formMapper: FormMapped[MyForm] = FormMapped.derived

  def printForm[F](formData: Map[String, Seq[String]])(using f: FormMapped[MyForm]): Unit =
    val mapped = f.mapForm(formData)
    println(mapped)

  printForm(
    Map(
      "firstName" -> Seq("Pero"),
      "lastName" -> Seq("Milikona"),
      "points" -> Seq("12", "44", "52"),
      "bornIn" -> Seq("Oct")
    )
  )
