package net.lambdaserve.jdbc

import scala.deriving.Mirror
import scala.compiletime.{constValue, erasedValue, summonInline}

import java.sql.ResultSet

object JdbcConverter:
  inline def fromJdbc[T](record: ResultSet)(using m: Mirror.ProductOf[T]): T =
    inline erasedValue[m.MirroredElemTypes] match
      case _: (t *: ts) =>
        val values =
          extractElements[m.MirroredElemTypes, m.MirroredElemLabels](record)
        m.fromProduct(Tuple.fromArray(values.toArray))

  inline def extractElements[T <: Tuple, L <: Tuple](
    record: ResultSet
  ): List[Any] =
    inline erasedValue[T] match
      case _: EmptyTuple => Nil
      case _: (t *: ts) =>
        inline erasedValue[L] match
          case _: (l *: ls) =>
            val label = constValue[l].toString
            val value = record.getObject(label)
            value :: extractElements[ts, ls](record)
