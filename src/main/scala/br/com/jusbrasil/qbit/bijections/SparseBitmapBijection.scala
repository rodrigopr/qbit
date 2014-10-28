package br.com.jusbrasil.qbit.bijections

import br.com.jusbrasil.qbit.bitmap.{BitmapInnerData, SparseBitmap}
import com.twitter.bijection.AbstractBijection
import org.jboss.netty.buffer.{ChannelBuffers, ChannelBuffer}

object SparseBitmapBijection extends AbstractBijection[SparseBitmap, ChannelBuffer] {
  override def apply(bitmap: SparseBitmap) = {
    val buffer = ChannelBuffers.buffer(bitmap.sizeInBytes)
    val data = bitmap.data()

    buffer.writeInt(data.numBuckets)
    writeLongArray(data.numBuckets, data.indexes, buffer)
    writeLongArray(data.numBuckets, data.values, buffer)
    buffer
  }

  override def invert(buffer: ChannelBuffer) = {
    val numBuckets = buffer.readInt()
    val indexes = readLongArray(numBuckets, buffer)
    val values = readLongArray(numBuckets, buffer)

    SparseBitmap(BitmapInnerData(indexes, values, numBuckets))
  }

  private def writeLongArray(length: Int, arr: Array[Long], buffer: ChannelBuffer) {
    var i = 0
    while (i < length) {
      buffer.writeLong(arr(i))
      i = i + 1
    }
  }

  private def readLongArray(length: Int, buffer: ChannelBuffer) = {
    var i = 0
    val arr: Array[Long] = new Array[Long](length)
    while (i < length) {
      arr(i) = buffer.readLong()
      i = i + 1
    }

    arr
  }
}
