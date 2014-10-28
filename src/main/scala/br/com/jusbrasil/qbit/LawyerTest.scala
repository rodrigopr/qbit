package br.com.jusbrasil.qbit

import br.com.jusbrasil.qbit.bitmap.{BitmapOperator, SparseBitmap}
import br.com.jusbrasil.qbit.storage.IndexStorageFactory
import com.twitter.storehaus.{ReadableStore, Store}
import com.twitter.util.{Future, Await}

import scala.collection.mutable
import scala.io.Source
import scala.util.Random


/**
 * Download the dataset from: http://d.pr/f/1ajDk
 *
 * Need a redis running in port 6379
 *
 * !! This is an example, not a benchmark !!
 */



object LawyerTest extends App {
  import ExperimentUtils._
  
  val indexesMap: Map[String, SparseBitmap] = loadDataset

  val store: Store[String, SparseBitmap] = IndexStorageFactory.store
  val cachedStore: ReadableStore[String, SparseBitmap] = IndexStorageFactory.cachedStore

  /**
   * Store the loaded dataset to storage(redis)
   */
  storeIndexes(store, indexesMap)


  /**
   * Real example that took > 1s to compute on today infra-estructure
   */
  val realExampleLatency = measure {
    val cityOrParentIdx = or(idx("26416439"), idx("26418608"))
    val locationAndSubject = and(idx("26413516"), cityOrParentIdx)
  }
  println("Real example latency: %.2f/ms".format(realExampleLatency))


  /**
   * Few random performance tests (testing latency from up to 100 index operations)
   */
  for(i ← 1 to 100){
    val allIndexesKey = indexesMap.keys.toVector
    val size = allIndexesKey.size

    val time = measure {
      randomQueries(i, store, () ⇒ { allIndexesKey(Random.nextInt(size)) })
    }

    println("It: %d, %.2f/ms".format(i, time))
  }

  /**
   * Helper functions (change to cachedStore to bust performance)
   */
  def idx(key: String): SparseBitmap = {
    Await.result(store.get(key)).getOrElse(SparseBitmap())
  }

  def or(idxs: SparseBitmap*) = BitmapOperator.or(idxs.toArray)
  
  def and(idxs: SparseBitmap*) = BitmapOperator.and(idxs.toArray)
}

object ExperimentUtils {
  def loadDataset: Map[String, SparseBitmap] = {
    val tmpIdxs = mutable.Map[String, SparseBitmap]()

    def set(key: String, id: Long): Unit = {
      val old = tmpIdxs.getOrElse(key, SparseBitmap())
      val newIndex = old.set(id)

      tmpIdxs.put(key, newIndex)
    }

    Source.fromFile("advs.txt").getLines().foreach { l ⇒
      val Array(perfil, loc, locPai, assunto, pro) = l.split(",")
      set(loc, perfil.toLong)
      set(locPai, perfil.toLong)
      set(assunto, perfil.toLong)

      if (pro.toBoolean) {
        set("pro", perfil.toLong)
      } else {
        set("free", perfil.toLong)
      }
    }

    println("Loaded dataset - Total size in bytes: " + tmpIdxs.values.map(_.sizeInBytes).sum)

    tmpIdxs.toMap
  }

  def storeIndexes(store: Store[String, SparseBitmap], idxs: Map[String, SparseBitmap]) {
    idxs.mapValues(Some(_)).foreach { k ⇒ Await.result(store.put(k))}

    println("Finished saving to datastore")
  }

  def randomQueries(numIndexes: Int, store: Store[String, SparseBitmap], randKeyGen: () ⇒ String): SparseBitmap = {
    val randomIndexes =
      for (i ← 1 to numIndexes) yield store.get(randKeyGen())

    val finalRes = Future.collect(randomIndexes) map { case ls ⇒
      ls.foldLeft(SparseBitmap()) { case (acc, idx) ⇒
        val rand = Random.nextInt(10)
        val function = rand match {
          case 0 ⇒ BitmapOperator.and _
          case 1 ⇒ BitmapOperator.xor _
          case _ ⇒ BitmapOperator.or _
        }

        function(Array(acc, idx.get))
      }
    }

    Await.result(finalRes)
  }

  def measure(fn: ⇒ Unit): Float = {
    var allTimes = List[Long]()
    for (j ← 1 to 10) {
      val start = System.currentTimeMillis ()
      fn
      val end = System.currentTimeMillis ()
      val time = end - start
      allTimes = time :: allTimes
    }
    allTimes.sum.toFloat / allTimes.size
  }
}
