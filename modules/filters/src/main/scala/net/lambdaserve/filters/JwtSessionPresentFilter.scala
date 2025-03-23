package net.lambdaserve.filters

class JwtSessionPresentFilter(
  redirectToIfNot: String,
  includePrefixes: List[String] = List(""),
  excludePrefixes: List[String] = List.empty
) extends PredicateRedirectFilter(! _.data.contains(JwtSession), redirectToIfNot)
