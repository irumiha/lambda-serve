package net.lambdaserve.mapextract

trait ContainerInstances:
  given seq[T](using sfm: MapExtract[T]): MapExtract[Seq[T]] with
    override def projectMap(
      m: Map[String, IndexedSeq[String]],
      prefix: String,
      offset: Int
    ): Seq[T] =
      m.keys.find(path => path.startsWith(prefix)) match
        case Some(key) =>
          m(key).indices.map { idx =>
            sfm.projectMap(m, prefix, idx)
          }
        case _ =>
          throw IllegalArgumentException(s"Field not found at path $prefix")

  given list[T](using sfm: MapExtract[Seq[T]]): MapExtract[List[T]] with
    override def projectMap(
      m: Map[String, IndexedSeq[String]],
      prefix: String,
      offset: Int
    ): List[T] =
      sfm.projectMap(m, prefix, offset).toList

  given vec[T](using sfm: MapExtract[Seq[T]]): MapExtract[Vector[T]] with
    override def projectMap(
      m: Map[String, IndexedSeq[String]],
      prefix: String,
      offset: Int
    ): Vector[T] =
      sfm.projectMap(m, prefix, offset).toVector

  given option[T](using sfm: MapExtract[T]): MapExtract[Option[T]] with
    override def projectMap(
      m: Map[String, IndexedSeq[String]],
      prefix: String,
      offset: Int
    ): Option[T] =
      m.keys.find(path => path.startsWith(prefix)) match
        case Some(prefKey) if m(prefKey).isEmpty => None
        case Some(prefKey) => Some(sfm.projectMap(m, prefix, offset))
        case _             => None
