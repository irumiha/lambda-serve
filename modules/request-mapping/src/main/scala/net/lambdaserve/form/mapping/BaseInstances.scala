package net.lambdaserve.form.mapping

import java.util.UUID

trait BaseInstances:
  given MapExtract[Short] with
    override def mapForm(
      m: Map[String, IndexedSeq[String]],
      prefix: String,
      offset: Int
    ): Short =
      MapExtract.extractString(m, prefix, offset).toShort

  given MapExtract[Int] with
    override def mapForm(
      m: Map[String, IndexedSeq[String]],
      prefix: String,
      offset: Int
    ): Int =
      MapExtract.extractString(m, prefix, offset).toInt

  given MapExtract[Float] with
    override def mapForm(
      m: Map[String, IndexedSeq[String]],
      prefix: String,
      offset: Int
    ): Float =
      MapExtract.extractString(m, prefix, offset).toFloat

  given MapExtract[Double] with
    override def mapForm(
      m: Map[String, IndexedSeq[String]],
      prefix: String,
      offset: Int
    ): Double =
      MapExtract.extractString(m, prefix, offset).toDouble

  given MapExtract[BigDecimal] with
    override def mapForm(
      m: Map[String, IndexedSeq[String]],
      prefix: String,
      offset: Int
    ): BigDecimal =
      BigDecimal(MapExtract.extractString(m, prefix, offset))

  given MapExtract[Boolean] with
    override def mapForm(
      m: Map[String, IndexedSeq[String]],
      prefix: String,
      offset: Int
    ): Boolean =
      MapExtract.extractString(m, prefix, offset).toBoolean

  given MapExtract[String] with
    override def mapForm(
      m: Map[String, IndexedSeq[String]],
      prefix: String,
      offset: Int
    ): String =
      MapExtract.extractString(m, prefix, offset)

  given MapExtract[UUID] with
    override def mapForm(
      m: Map[String, IndexedSeq[String]],
      prefix: String,
      offset: Int
    ): UUID =
      UUID.fromString(MapExtract.extractString(m, prefix, offset))
