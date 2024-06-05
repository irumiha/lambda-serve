package net.lambdaserve.mapextract

trait ContainerInstances:
  given seq[T](using sfm: MapExtract[T]): MapExtract[Seq[T]] with
    override def projectMaps(
      ms: Seq[Map[String, IndexedSeq[String]]],
      prefix: String,
      offset: Int
    ): Seq[T] =
      ms.find(it => it.keys.exists(path => path.startsWith(prefix)))
        .flatMap { it =>
          it.keys
            .find(path => path.startsWith(prefix))
            .map(key =>
              it(key).indices.map { idx =>
                sfm.projectMaps(Seq(it), prefix, idx)
              }
            )
        }
        .getOrElse(
          throw IllegalArgumentException(s"Field not found at path $prefix")
        )

  given list[T](using sfm: MapExtract[Seq[T]]): MapExtract[List[T]] with
    override def projectMaps(
      ms: Seq[Map[String, IndexedSeq[String]]],
      prefix: String,
      offset: Int
    ): List[T] =
      sfm.projectMaps(ms, prefix, offset).toList

  given vec[T](using sfm: MapExtract[Seq[T]]): MapExtract[Vector[T]] with
    override def projectMaps(
      ms: Seq[Map[String, IndexedSeq[String]]],
      prefix: String,
      offset: Int
    ): Vector[T] =
      sfm.projectMaps(ms, prefix, offset).toVector

  given option[T](using sfm: MapExtract[T]): MapExtract[Option[T]] with
    override def projectMaps(
      ms: Seq[Map[String, IndexedSeq[String]]],
      prefix: String,
      offset: Int
    ): Option[T] =
      ms.find(it => it.keys.exists(path => path.startsWith(prefix)))
        .flatMap { it =>
          it.keys.find(path => path.startsWith(prefix)) match
            case Some(prefKey) if it(prefKey).isEmpty => None
            case Some(prefKey) => Some(sfm.projectMaps(ms, prefix, offset))
            case _             => None

        }
