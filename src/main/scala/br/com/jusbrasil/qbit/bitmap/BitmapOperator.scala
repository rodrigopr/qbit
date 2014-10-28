package br.com.jusbrasil.qbit.bitmap

import java.util.{Comparator, PriorityQueue}

object BitmapOperator {
  def and(indexes: Array[SparseBitmap]) = apply(and2x2)(indexes)
  def or(indexes: Array[SparseBitmap]) = apply(general2x2(_ | _))(indexes)
  def xor(indexes: Array[SparseBitmap]) = apply(general2x2(_ ^ _))(indexes)

  private def apply(fn: (SparseBitmap, SparseBitmap) => SparseBitmap)(indexes: Array[SparseBitmap]) = {
    val pq = new PriorityQueue[SparseBitmap](indexes.length, new Comparator[SparseBitmap] {
      def compare(o1: SparseBitmap, o2: SparseBitmap): Int = o1.numBuckets.compareTo(o2.numBuckets)
    })

    indexes.foreach{ idx =>
      if (idx.numBuckets > 0) {
        pq.add(idx)
      }
    }

    while(pq.size() > 1) {
      val idx1 = pq.poll()
      val idx2 = pq.poll()

      pq.add(fn(idx1, idx2))
    }

    pq.poll()
  }

  private def and2x2(idx1: SparseBitmap, idx2: SparseBitmap): SparseBitmap = {
    if(Math.min(idx1.numBuckets, idx2.numBuckets) == 0) {
      SparseBitmap()
    } else {
      val estimatedSize = Math.min(idx1.numBuckets, idx2.numBuckets)
      val fastBitmap = new FastBitmap(estimatedSize)

      val it1 = idx1.dataIterator
      val it2 = idx2.dataIterator

      while(it1.hasNext && it2.hasNext) {
        it1.compareTo(it2) match {
          case -1 =>
            it1.moveForward()

          case 1 =>
            it2.moveForward()

          case _ =>
            fastBitmap.fastAdd(it1.currentIdx, it1.currentValue & it2.currentValue)
            it2.moveForward()
            it1.moveForward()
        }
      }
      fastBitmap.finalBitmap()
    }
  }

  private def general2x2(fn: (Long, Long) => Long)(idx1: SparseBitmap, idx2: SparseBitmap): SparseBitmap = {
    val estimatedSize = 3 * (idx1.numBuckets + idx2.numBuckets) / 4
    val fastBitmap = new FastBitmap(estimatedSize)

    val it1 = idx1.dataIterator
    val it2 = idx2.dataIterator

    while(it1.hasNext && it2.hasNext) {
      it1.compareTo(it2) match {
        case -1 =>
          fastBitmap.fastAdd(it1.currentIdx, it1.currentValue)
          it1.moveForward()

        case 1 =>
          fastBitmap.fastAdd(it2.currentIdx, it2.currentValue)
          it2.moveForward()

        case _ =>
          fastBitmap.fastAdd(it1.currentIdx, fn(it1.currentValue, it2.currentValue))
          it2.moveForward()
          it1.moveForward()
      }
    }

    consumeAllRemain(it1, fastBitmap)
    consumeAllRemain(it2, fastBitmap)

    fastBitmap.finalBitmap()
  }

  private def consumeAllRemain(from: BitmapDataIterator, to: FastBitmap) {
    while (from.hasNext) {
      to.fastAdd(from.currentIdx, from.currentValue)
      from.moveForward()
    }
  }
}
