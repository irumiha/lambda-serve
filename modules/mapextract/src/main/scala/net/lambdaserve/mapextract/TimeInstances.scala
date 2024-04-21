package net.lambdaserve.mapextract

import java.time.{Instant, LocalDate, LocalDateTime, ZonedDateTime}

trait TimeInstances:

  given MapExtract[LocalDateTime] with
    override def projectMap(
      m: Map[String, IndexedSeq[String]],
      prefix: String,
      offset: Int
    ): LocalDateTime =
      LocalDateTime.parse(MapExtract.extractString(m, prefix, offset))

  given MapExtract[ZonedDateTime] with
    override def projectMap(
      m: Map[String, IndexedSeq[String]],
      prefix: String,
      offset: Int
    ): ZonedDateTime =
      ZonedDateTime.parse(MapExtract.extractString(m, prefix, offset))

  given MapExtract[Instant] with
    override def projectMap(
      m: Map[String, IndexedSeq[String]],
      prefix: String,
      offset: Int
    ): Instant =
      Instant.parse(MapExtract.extractString(m, prefix, offset))

  given MapExtract[LocalDate] with
    override def projectMap(
      m: Map[String, IndexedSeq[String]],
      prefix: String,
      offset: Int
    ): LocalDate =
      LocalDate.parse(MapExtract.extractString(m, prefix, offset))

object TimeInstances extends TimeInstances
