package br.com.jusbrasil.qbit.bitmap

import br.com.jusbrasil.qbit.utils.{SubArray, ArraysUtils}
import java.util

class SparseBitmap(indexes: Array[Long], values: Array[Long], val numBuckets: Int) extends Iterable[Long] {
  def data() = BitmapInnerData(indexes, values, numBuckets)

  def cardinality = 0.until(numBuckets).map(i => java.lang.Long.bitCount(values(i))).sum

  def sizeInBytes = 4 + (numBuckets * 8 * 2)

  def get(pos: Long): Boolean = {
    val bucket = SparseBitmap.bucketFor(pos)
    val indexPos = util.Arrays.binarySearch(indexes, 0, numBuckets, bucket)

    if(indexPos < 0) {
      false
    } else {
      val bits = values(indexPos)
      (bits & SparseBitmap.bucketBitFor(pos)) != 0
    }
  }

  def unset(pos: Long): SparseBitmap = set(pos, active = false)
  
  def set(pos: Long): SparseBitmap = set(pos, active = true)

  def set(pos: Long, active: Boolean): SparseBitmap = {
    val bucket = SparseBitmap.bucketFor(pos)
    val indexPos = util.Arrays.binarySearch(indexes, 0, numBuckets, bucket)

    val op = SparseBitmap.bucketBitFor(pos)
    if(indexPos < 0) {
      if(active) {
        val splitPosition = Math.abs(indexPos + 1)
        val takeFromRight = indexes.length - splitPosition

        val newIndexes = ArraysUtils.fastUnion(indexes.length + 1,
          SubArray(indexes, 0, splitPosition),
          SubArray(Array(bucket)),
          SubArray(indexes, numBuckets - takeFromRight, numBuckets)
        )

        val newValues = ArraysUtils.fastUnion(indexes.length + 1,
          SubArray(values, 0, splitPosition),
          SubArray(Array(op)),
          SubArray(values, numBuckets - takeFromRight, numBuckets)
        )

        new SparseBitmap(newIndexes, newValues, numBuckets + 1)
      } else this
    } else {
      val value = values(indexPos)

      val newValues = util.Arrays.copyOf(values, values.length)
      newValues(indexPos) = if(active) value | op else value ^ op

      new SparseBitmap(indexes, newValues, newValues.length)
    }
  }

  def iterator = new Iterator[Long]{
    var i = 0
    var currentWord = if (numBuckets == 0) 0 else values(0)
    var base = if (numBuckets == 0) 0 else indexes(i) * 64

    def hasNext: Boolean = currentWord != 0

    def next(): Long = {
      val lowestBit = currentWord & (-currentWord)
      currentWord ^= lowestBit

      val answer = base + java.lang.Long.bitCount(lowestBit - 1)

      while (currentWord == 0 && i < (numBuckets-1)) {
        i += 1
        currentWord = values(i)
        base = indexes(i) * 64
      }

      answer
    }
  }

  private[bitmap] def dataIterator = new BitmapDataIterator(indexes, values, numBuckets)
}

object SparseBitmap {
  def apply() = new SparseBitmap(Array(), Array(), 0)

  def apply(data: BitmapInnerData) = new SparseBitmap(data.indexes, data.values, data.numBuckets)

  def apply(all: Array[Long]) = {
    val estimatedNewSize = all.length / 8
    val fastBitmap = new FastBitmap(estimatedNewSize)

    all.foreach(pos => fastBitmap.fastOr(bucketFor(pos), bucketBitFor(pos)))

    fastBitmap.finalBitmap()
  }

  private[bitmap] def bucketFor(pos: Long) = (pos / 64) - (if(pos < 0x0) 1 else 0)
  private[bitmap] def bucketBitFor(pos: Long): Long = 1l << (pos % 64)
}

