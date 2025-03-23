package net.lambdaserve

class TypedKey[A]:
  def get(m: Map[TypedKey[_], Any]): Option[A] =
    m.get(this).map(_.asInstanceOf[A])

  def apply(m: Map[TypedKey[_], Any]): A =
    m.get(this)
      .map(_.asInstanceOf[A])
      .getOrElse(throw new NoSuchElementException)

  def set(m: Map[TypedKey[_], Any], value: A): Map[TypedKey[_], Any] =
    m.updated(this, value)

class TypedMap:
  private var internalMap: Map[TypedKey[_], Any] = Map.empty

  def get[A](key: TypedKey[A]): Option[A] = key.get(internalMap)

  def apply[A](key: TypedKey[A]): A = key(internalMap)

  def set[A](key: TypedKey[A], value: A): TypedMap =
    internalMap = key.set(internalMap, value)
    this

  def remove[A](key: TypedKey[A]): TypedMap =
    internalMap = internalMap - key
    this

  def contains[A](key: TypedKey[A]): Boolean = internalMap.contains(key)

  override def toString: String = internalMap.toString
