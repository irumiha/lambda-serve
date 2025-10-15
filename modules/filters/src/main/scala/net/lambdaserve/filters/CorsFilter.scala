package net.lambdaserve.filters

import net.lambdaserve.http.{Header, Method, Request, Response, Status}

/** CORS (Cross-Origin Resource Sharing) filter that handles preflight requests
  * and adds appropriate CORS headers to responses.
  *
  * @param allowedOrigins
  *   Origins that are allowed to make cross-origin requests. Use "*" for all
  *   origins, or specify exact origins like "https://example.com". Multiple
  *   origins can be specified.
  * @param allowedMethods
  *   HTTP methods that are allowed for cross-origin requests
  * @param allowedHeaders
  *   Headers that are allowed in cross-origin requests
  * @param exposedHeaders
  *   Headers that can be exposed to the browser
  * @param allowCredentials
  *   Whether credentials (cookies, authorization headers) are allowed
  * @param maxAge
  *   How long (in seconds) the preflight response can be cached
  * @param includePrefixes
  *   URL prefixes to apply this filter to (default: all paths)
  * @param excludePrefixes
  *   URL prefixes to exclude from this filter
  */
class CorsFilter(
  val allowedOrigins: Set[String] = Set("*"),
  val allowedMethods: Set[Method] = Set(
    Method.GET,
    Method.POST,
    Method.PUT,
    Method.PATCH,
    Method.DELETE,
    Method.OPTIONS
  ),
  val allowedHeaders: Set[String] = Set(
    "Content-Type",
    "Authorization",
    "X-Requested-With"
  ),
  val exposedHeaders: Set[String] = Set.empty,
  val allowCredentials: Boolean = false,
  val maxAge: Option[Int] = Some(3600),
  override val includePrefixes: List[String] = List(""),
  override val excludePrefixes: List[String] = List.empty
) extends Filter:

  private val allowAllOrigins = allowedOrigins.contains("*")

  private def isOriginAllowed(origin: String): Boolean =
    allowAllOrigins || allowedOrigins.contains(origin)

  private def getAllowOriginHeader(requestOrigin: Option[String]): Option[String] =
    requestOrigin match
      case Some(origin) if isOriginAllowed(origin) =>
        // When credentials are allowed, we must return the specific origin, not "*"
        if allowCredentials then Some(origin)
        else if allowAllOrigins then Some("*")
        else Some(origin)
      case _ => None

  private def addCorsHeaders(response: Response, origin: Option[String]): Response =
    getAllowOriginHeader(origin) match
      case Some(allowOriginValue) =>
        var updatedResponse = response
          .addHeader(Header.AccessControlAllowOrigin.name, allowOriginValue)

        if allowCredentials then
          updatedResponse = updatedResponse
            .addHeader(Header.AccessControlAllowCredentials.name, "true")

        if exposedHeaders.nonEmpty then
          updatedResponse = updatedResponse
            .addHeader(
              Header.AccessControlExposeHeaders.name,
              exposedHeaders.mkString(", ")
            )

        updatedResponse

      case None => response

  private def handlePreflightRequest(request: Request): Response =
    val origin = request.headers.get(Header.Origin.name).flatMap(_.headOption)

    getAllowOriginHeader(origin) match
      case Some(allowOriginValue) =>
        var response = Response(
          status = Status.NoContent,
          headers = Map(
            Header.AccessControlAllowOrigin.name -> Seq(allowOriginValue),
            Header.AccessControlAllowMethods.name -> Seq(
              allowedMethods.map(_.toString).mkString(", ")
            ),
            Header.AccessControlAllowHeaders.name -> Seq(
              allowedHeaders.mkString(", ")
            )
          ),
          bodyWriter = _ => ()
        )

        if allowCredentials then
          response = response
            .addHeader(Header.AccessControlAllowCredentials.name, "true")

        maxAge.foreach { age =>
          response = response
            .addHeader(Header.AccessControlMaxAge.name, age.toString)
        }

        response

      case None =>
        // Origin is not allowed, return 403
        Response(
          status = Status.Forbidden,
          headers = Map.empty,
          bodyWriter = _ => ()
        )

  override def handle(request: Request): FilterInResponse =
    val origin = request.headers.get(Header.Origin.name).flatMap(_.headOption)

    // Handle preflight OPTIONS request
    if request.method == Method.OPTIONS then
      FilterInResponse.Stop(handlePreflightRequest(request))
    else
      // For actual requests, wrap the response to add CORS headers
      FilterInResponse.Wrap(
        request,
        response => FilterOutResponse.Continue(addCorsHeaders(response, origin))
      )
