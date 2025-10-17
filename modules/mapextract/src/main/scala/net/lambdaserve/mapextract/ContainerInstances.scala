package net.lambdaserve.mapextract

trait ContainerInstances:
  given seq[T](using sfm: MapExtract[T]): MapExtract[Seq[T]] with
    override def projectMaps(
      ms: Seq[Map[String, IndexedSeq[String]]],
      prefix: String,
      offset: Int
    ): Seq[T] =
      // Helper function to check if a key matches the prefix pattern
      def matchesPrefix(key: String): Boolean =
        key == prefix || key.startsWith(s"$prefix.") || key.startsWith(s"$prefix[")

      // Check if any map contains keys with indexed notation or regular prefix
      val mapOpt = ms.find(it =>
        it.keys.exists(matchesPrefix)
      )

      mapOpt match
        case Some(it) =>
          // Extract indexed keys like prefix[0], prefix[1], etc.
          val indexedKeys = it.keys
            .filter(key => key.startsWith(s"$prefix["))
            .flatMap { key =>
              // Extract index from prefix[idx] or prefix[idx].field
              val pattern = s"\\Q$prefix\\E\\[(\\d+)\\]".r
              pattern.findFirstMatchIn(key).map(_.group(1).toInt)
            }
            .toSet
            .toSeq
            .sorted

          if indexedKeys.nonEmpty then
            // Use indexed notation: prefix[0].field, prefix[1].field, etc.
            indexedKeys.map { idx =>
              val indexedPrefix = s"$prefix[$idx]"
              sfm.projectMaps(Seq(it), indexedPrefix, 0)
            }
          else
            // Fallback to old behavior: prefix.field with multiple values
            it.keys
              .find(matchesPrefix)
              .map(key =>
                it(key).indices.map { idx =>
                  sfm.projectMaps(Seq(it), prefix, idx)
                }
              )
              .getOrElse(Seq.empty)
        case None =>
          throw IllegalArgumentException(s"Field not found at path $prefix")

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
            case Some(_) => Some(sfm.projectMaps(ms, prefix, offset))
            case _       => None

        }
