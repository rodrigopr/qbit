package br.com.jusbrasil.qbit.index

import akka.routing.ConsistentHashingRouter.ConsistentHashable


sealed trait IndexOperation
object AddOperation extends IndexOperation
object RemoveOperation extends IndexOperation

case class IndexTask(value: Long, op: IndexOperation)

case class IndexBatchTask(indexName: String, tasks: List[IndexTask]) extends ConsistentHashable {
  override def consistentHashKey: Any = indexName
}
