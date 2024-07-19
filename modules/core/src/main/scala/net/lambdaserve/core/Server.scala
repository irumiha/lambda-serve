package net.lambdaserve.core

import net.lambdaserve.core.filters.Filter

trait Server[S, H]:
  def makeServer(
    host: String,
    port: Int,
    router: Router,
    filters: IndexedSeq[Filter],
    staticPaths: List[String],
    staticPrefix: Option[String],
    gzipSupport: Boolean,
    limitRequestSize: Long,
    limitResponseSize: Long,
    useVirtualTheads: Boolean,
  ): S

  def addToConfiguredServer(router: Router, filters: IndexedSeq[Filter])(c: H => S): S
