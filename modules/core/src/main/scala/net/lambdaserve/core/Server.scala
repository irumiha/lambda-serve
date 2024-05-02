package net.lambdaserve.core

import net.lambdaserve.core.filters.Filter

trait Server[S]:
  def makeServer(
    host: String,
    port: Int,
    router: Router,
    filters: IndexedSeq[Filter]
  ): S
