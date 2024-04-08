package net.lambdaserve.form.mapping

trait BaseInstances:
  given FormMapped[Short] with
    override def mapForm(m: Map[String, IndexedSeq[String]], prefix: String, offset: Int): Short =
      FormMapped.extractString(m, prefix, offset).toShort

  given FormMapped[Int] with
    override def mapForm(m: Map[String, IndexedSeq[String]], prefix: String, offset: Int): Int =
      FormMapped.extractString(m, prefix, offset).toInt

  given FormMapped[Float] with
    override def mapForm(m: Map[String, IndexedSeq[String]], prefix: String, offset: Int): Float =
      FormMapped.extractString(m, prefix, offset).toFloat

  given FormMapped[Double] with
    override def mapForm(m: Map[String, IndexedSeq[String]], prefix: String, offset: Int): Double =
      FormMapped.extractString(m, prefix, offset).toDouble

  given FormMapped[BigDecimal] with
    override def mapForm(
      m: Map[String, IndexedSeq[String]],
      prefix: String,
      offset: Int
    ): BigDecimal =
      BigDecimal(FormMapped.extractString(m, prefix, offset))

  given FormMapped[Boolean] with
    override def mapForm(m: Map[String, IndexedSeq[String]], prefix: String, offset: Int): Boolean =
      FormMapped.extractString(m, prefix, offset).toBoolean

  given FormMapped[String] with
    override def mapForm(m: Map[String, IndexedSeq[String]], prefix: String, offset: Int): String =
      FormMapped.extractString(m, prefix, offset)
