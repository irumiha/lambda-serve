package net.lambdaserve.jwt.filter
import net.lambdaserve.filters.PredicateRedirectFilter

class JwtSessionPresentFilter(
  redirectToIfNot: String,
  override val includePrefixes: List[String] = List(""),
  override val excludePrefixes: List[String] = List.empty
) extends PredicateRedirectFilter(!_.data.contains(JwtSession), redirectToIfNot)
