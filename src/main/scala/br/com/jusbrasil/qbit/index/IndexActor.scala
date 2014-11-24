package br.com.jusbrasil.qbit.index

import akka.actor.Actor
import br.com.jusbrasil.qbit.bitmap.SparseBitmap
import br.com.jusbrasil.qbit.storage.IndexStorageFactory
import com.twitter.util.{Await, Future}
import org.slf4j.LoggerFactory


class IndexActor extends Actor {
  private val logger = LoggerFactory.getLogger(classOf[IndexActor])
  private val indexStore = IndexStorageFactory.store

  def receive = {
    case IndexBatchTask(indexName, tasks) =>
      logger.info(s"Received: $indexName, $tasks")
      val response = processBatch(indexName, tasks)

      // TODO: it should be totally asynchronous!
      // but as it is, we could introduce an race condition if we remove the await
      // We could try with a queue per index, and process the queue as a batch each iteration.
      Await.ready(response)
  }

  /**
   * Apply operations in index and update it in the data store,
   * returns future indicating the completion
   */
  def processBatch(indexName: String, tasks: List[IndexTask]): Future[Unit] = {
    val t0 = System.currentTimeMillis

    val response = indexStore.get(indexName) flatMap { loaded =>
      val originalBitmap = loaded.getOrElse(SparseBitmap())

      val (needUpdate, finalBitmap) = tasks.foldLeft((false, originalBitmap)) {
        case ((hasChanges, bitmap), IndexTask(id, op)) ⇒
          val (updated, updatedBitmap) = applyOperation(bitmap, id, op)

          (hasChanges || updated, updatedBitmap)
      }

      if (needUpdate) {
        indexStore.put(indexName → Some(finalBitmap))
      } else {
        Future.value()
      }

    }

    response onSuccess { _ ⇒
      val deltams = System.currentTimeMillis - t0
      logger.info(s"Indexed ${tasks.size} operations in $indexName in $deltams ms")
    } onFailure { ex ⇒
      logger.error(s"Failure to index: $indexName", ex)
    }

    response
  }

  /**
   * Apply index operator to the loaded index bitmap
   * Returns a tuple with: (boolean indicating if the bitmap has changed, the resulting bitmap)
   */
  def applyOperation(bitmap: SparseBitmap, id: Long, op: IndexOperation): (Boolean, SparseBitmap) = {
    val existInBitmap = bitmap.get(id)

    val (updated, updatedBitmap) = op match {
      case AddOperation if !existInBitmap ⇒
        (true, bitmap.set(id))

      case RemoveOperation if existInBitmap ⇒
        (true, bitmap.unset(id))

      case _ ⇒
        (false, bitmap)
    }

    (updated, updatedBitmap)
  }
}
