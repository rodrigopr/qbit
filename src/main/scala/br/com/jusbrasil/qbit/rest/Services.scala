package br.com.jusbrasil.qbit.rest

import br.com.jusbrasil.qbit.index.{IndexBatchTask, Indexer, IndexTask, IndexOperation}
import br.com.jusbrasil.qbit.storage.IndexStorageFactory
import com.twitter.finagle.Service
import com.twitter.finagle.stats.LoadedStatsReceiver
import com.twitter.util.Future
import io.finch._
import io.finch.request._
import org.jboss.netty.handler.codec.http.HttpResponseStatus._

object Services {
  private val stats = LoadedStatsReceiver.scope("services")
  
  private val indexer = new Indexer(10)
  private val store = IndexStorageFactory.store
  private val cache = IndexStorageFactory.cachedStore

  /**
   * Send index request batch to indexer subsystem
   */
  case class IndexRequest(indexName: String, operation: IndexOperation) extends Service[HttpRequest, RestResponse] {
    override def apply(request: HttpRequest): Future[RestResponse] = delegate(request, "index-request") {
      for(ids <- RequiredLongParams("id")) yield {
        val tasks = ids.map { id => IndexTask(id, operation) }
        val batch = IndexBatchTask(indexName, tasks)

        indexer.send(batch)

        RestResponse(ACCEPTED, "Request queued")
      }
    }
  }

  /**
   * Return information about a particular index
   */
  case class GetInfo(indexName: String) extends Service[HttpRequest, RestResponse] {
    override def apply(request: HttpRequest): Future[RestResponse] = measure("get-info") {
      val debug = request.params.getBooleanOrElse("debug", default = false)
      
      for {
        indexOpt <- store.get(indexName)
      } yield {
        
        indexOpt match {
          case None => 
            RestResponse(NOT_FOUND, "Index not found")
            
          case Some(index) =>
            def debugInfo = {
              if(debug)
                s"All ids: ${index.iterator.toList}\n"
              else
                ""
            }
            
            val info =
              s"""
                 |name: $indexName
                 |number of buckets: ${index.numBuckets} 
                 |size: ${index.size}
                 |size in bytes: ${index.sizeInBytes}
                 |
                 |$debugInfo
               """.stripMargin
            
            RestResponse(OK, info)
        }
      }
    }
  }
  
  private def delegate[A](httpRequest: HttpRequest, id: String)(fn: ⇒ RequestReader[A]): Future[A] = measure(id) {
    fn(httpRequest)
  }

  private def measure[T](id: String)(fn: => Future[T]): Future[T] = {
    stats.timeFuture(id, "ms")(fn)
      .onSuccess { _ => stats.counter(id, "success").incr()}
      .onFailure { _ => stats.counter(id, "failure").incr()}
  }
}
