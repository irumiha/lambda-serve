package net.lambdaserve.core

trait Server[S]:
  def makeServer(host: String, port: Int, router: Router): S
