package net.lambdaserve.mapextract

import java.util.UUID

trait BaseInstances:
  given MapExtract[Short] with
    override def projectMaps(
      m: Seq[Map[String, IndexedSeq[String]]],
      prefix: String,
      offset: Int
    ): Short =
      MapExtract.extractString(m, prefix, offset).toShort

  given MapExtract[Int] with
    override def projectMaps(
      m: Seq[Map[String, IndexedSeq[String]]],
      prefix: String,
      offset: Int
    ): Int =
      MapExtract.extractString(m, prefix, offset).toInt

  given MapExtract[Long] with
    override def projectMaps(
      m: Seq[Map[String, IndexedSeq[String]]],
      prefix: String,
      offset: Int
    ): Long =
      MapExtract.extractString(m, prefix, offset).toLong

  given MapExtract[Float] with
    override def projectMaps(
      m: Seq[Map[String, IndexedSeq[String]]],
      prefix: String,
      offset: Int
    ): Float =
      MapExtract.extractString(m, prefix, offset).toFloat

  given MapExtract[Double] with
    override def projectMaps(
      m: Seq[Map[String, IndexedSeq[String]]],
      prefix: String,
      offset: Int
    ): Double =
      MapExtract.extractString(m, prefix, offset).toDouble

  given MapExtract[BigDecimal] with
    override def projectMaps(
      m: Seq[Map[String, IndexedSeq[String]]],
      prefix: String,
      offset: Int
    ): BigDecimal =
      BigDecimal(MapExtract.extractString(m, prefix, offset))

  given MapExtract[Boolean] with
    override def projectMaps(
      m: Seq[Map[String, IndexedSeq[String]]],
      prefix: String,
      offset: Int
    ): Boolean =
      MapExtract.extractString(m, prefix, offset).toBoolean

  given MapExtract[String] with
    override def projectMaps(
      m: Seq[Map[String, IndexedSeq[String]]],
      prefix: String,
      offset: Int
    ): String =
      MapExtract.extractString(m, prefix, offset)

  given MapExtract[UUID] with
    override def projectMaps(
      m: Seq[Map[String, IndexedSeq[String]]],
      prefix: String,
      offset: Int
    ): UUID =
      UUID.fromString(MapExtract.extractString(m, prefix, offset))
