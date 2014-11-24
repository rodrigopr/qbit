package br.com.jusbrasil.qbit.rest

import com.twitter.app.App
import com.twitter.finagle.Service
import com.twitter.finagle.http.HttpMuxer
import com.twitter.server._
import com.twitter.util.Await
import io.finch._


trait BaseFinagleServer extends App
  with Hooks
  with AdminHttpServer
  with Admin
  with Lifecycle
  with Stats

object RestServer extends BaseFinagleServer {
  val service = buildService

  def main() {
    HttpMuxer.addRichHandler("/", service)

    Await.ready(adminHttpServer)
  }

  def buildService: Service[HttpRequest, HttpResponse] = {
    val backend = IndexEndpoint orElse Endpoint.NotFound
    val logger = new LoggingFilter

    logger andThen backend.toService
  }
}

