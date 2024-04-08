package net.lambdaserve.form.mapping

import java.time.{Instant, LocalDate, LocalDateTime, ZonedDateTime}

trait TimeInstances:

  given FormMapped[LocalDateTime] with
    override def mapForm(
      m: Map[String, IndexedSeq[String]],
      prefix: String,
      offset: Int
    ): LocalDateTime =
      LocalDateTime.parse(FormMapped.extractString(m, prefix, offset))

  given FormMapped[ZonedDateTime] with
    override def mapForm(
      m: Map[String, IndexedSeq[String]],
      prefix: String,
      offset: Int
    ): ZonedDateTime =
      ZonedDateTime.parse(FormMapped.extractString(m, prefix, offset))

  given FormMapped[Instant] with
    override def mapForm(
      m: Map[String, IndexedSeq[String]],
      prefix: String,
      offset: Int
    ): Instant =
      Instant.parse(FormMapped.extractString(m, prefix, offset))

  given FormMapped[LocalDate] with
    override def mapForm(
      m: Map[String, IndexedSeq[String]],
      prefix: String,
      offset: Int
    ): LocalDate =
      LocalDate.parse(FormMapped.extractString(m, prefix, offset))

object TimeInstances extends TimeInstances
