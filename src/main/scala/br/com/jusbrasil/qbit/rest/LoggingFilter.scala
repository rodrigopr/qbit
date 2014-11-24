package br.com.jusbrasil.qbit.rest

import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Future
import io.finch._
import org.slf4j.LoggerFactory

class LoggingFilter extends SimpleFilter[HttpRequest, HttpResponse] {
  private val logger = LoggerFactory.getLogger(classOf[LoggingFilter])

  def apply(request: HttpRequest, service: Service[HttpRequest, HttpResponse]): Future[HttpResponse] = {
    val start = System.currentTimeMillis()
    service(request) map { response =>
      val end = System.currentTimeMillis()
      val duration = end - start
      logger.info("%s %s %d %dms".format(request.method, request.uri, response.statusCode, duration))
      response
    }
  }
}

