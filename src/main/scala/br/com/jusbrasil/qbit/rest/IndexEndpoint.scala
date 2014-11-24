package br.com.jusbrasil.qbit.rest

import br.com.jusbrasil.qbit.index.{AddOperation, IndexOperation, RemoveOperation}
import com.twitter.finagle.Service
import com.twitter.finagle.http.path.{Path, Root, _}
import com.twitter.finagle.http.{Method, Request, Response}
import io.finch._
import org.jboss.netty.handler.codec.http.HttpMethod


object IndexEndpoint extends Endpoint[Request, Response] {
  override def route: PartialFunction[(HttpMethod, Path), Service[Request, Response]] = {
    case Method.Put -> Root / "index" / indexName / operation =>
      Services.IndexRequest(indexName, decode(operation)) ! RestResponseToHttp

    case Method.Get -> Root / "index" / indexName / "info" =>
      Services.GetInfo(indexName) ! RestResponseToHttp
  }

  def decode(operation: String): IndexOperation = {
    val op = if (operation.toLowerCase != "set") RemoveOperation else AddOperation
    op
  }
}
