package br.com.jusbrasil.qbit.rest

import com.twitter.finagle.Service
import io.finch._
import io.finch.response.Respond
import org.jboss.netty.handler.codec.http.HttpResponseStatus


case class RestResponse(status: HttpResponseStatus, content: String)

object RestResponseToHttp extends Service[RestResponse, HttpResponse] {
  def apply(resp: RestResponse) = {
    Respond(resp.status)(resp.content).toFuture
  }
}
