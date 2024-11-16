package net.lambdaserve.http

class TypedKey[A]:
  def get(m: Map[TypedKey[_], Any]): Option[A] =
    m.get(this).map(_.asInstanceOf[A])

  def apply(m: Map[TypedKey[_], Any]): A = m
    .get(this)
    .map(_.asInstanceOf[A])
    .getOrElse(throw new NoSuchElementException)

  def set(m: Map[TypedKey[_], Any], value: A): Map[TypedKey[_], Any] =
    m.updated(this, value)
