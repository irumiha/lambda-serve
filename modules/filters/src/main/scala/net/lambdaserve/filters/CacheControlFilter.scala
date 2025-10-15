package net.lambdaserve.filters

import net.lambdaserve.http.Request

/** Filter that adds cache control headers to responses.
  *
  * Useful for controlling browser and proxy caching behavior. Can be configured
  * for static assets, API responses, or other content types.
  *
  * @param cacheControl
  *   Cache-Control header value (e.g., "public, max-age=3600" or "no-cache,
  *   no-store")
  * @param expires
  *   Optional Expires header value (e.g., "Tue, 21 Oct 2025 07:28:00 GMT")
  * @param eTag
  *   Optional function to generate ETag value based on request
  * @param includePrefixes
  *   URL prefixes to apply this filter to (default: all paths)
  * @param excludePrefixes
  *   URL prefixes to exclude from this filter
  */
class CacheControlFilter(
  val cacheControl: String,
  val expires: Option[String] = None,
  val eTag: Option[Request => String] = None,
  override val includePrefixes: List[String] = List(""),
  override val excludePrefixes: List[String] = List.empty
) extends Filter:

  override def handle(request: Request): FilterInResponse =
    FilterInResponse.Wrap(
      request,
      response =>
        var updatedResponse = response.addHeader("Cache-Control", cacheControl)

        expires.foreach { exp =>
          updatedResponse = updatedResponse.addHeader("Expires", exp)
        }

        eTag.foreach { eTagGenerator =>
          val eTagValue = eTagGenerator(request)
          updatedResponse = updatedResponse.addHeader("ETag", eTagValue)
        }

        FilterOutResponse.Continue(updatedResponse)
    )

end CacheControlFilter

object CacheControlFilter:
  /** Creates a filter for static assets with aggressive caching */
  def forStaticAssets(maxAgeSeconds: Int = 31536000): CacheControlFilter =
    CacheControlFilter(
      cacheControl = s"public, max-age=$maxAgeSeconds, immutable",
      includePrefixes = List("/static", "/assets")
    )

  /** Creates a filter that disables caching (useful for API endpoints) */
  def noCache(): CacheControlFilter =
    CacheControlFilter(
      cacheControl = "no-cache, no-store, must-revalidate",
      expires = Some("0")
    )

  /** Creates a filter for private content with short-term caching */
  def forPrivateContent(maxAgeSeconds: Int = 300): CacheControlFilter =
    CacheControlFilter(cacheControl = s"private, max-age=$maxAgeSeconds")
