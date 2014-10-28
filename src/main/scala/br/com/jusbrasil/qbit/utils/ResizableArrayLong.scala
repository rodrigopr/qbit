package br.com.jusbrasil.qbit.utils

import java.util

/**
 * Created to use a long[] directly, instead of Long[],
 * avoiding unwanted boxing/unboxing
 */
class ResizableArrayLong(estimatedSize: Int) {
  private var array = new Array[Long](estimatedSize)
  private var innerSize = 0

  def innerArray: Array[Long] = this.array
  def size = innerSize

  def append(v: Long) {
    ensureSize(innerSize + 1)
    array(innerSize) = v
    innerSize += 1
  }

  protected def ensureSize(n: Int) {
    if (n > array.length) {
      var newsize = Math.max(array.length, 4)
      while (n > newsize) {
        if(newsize < 1024) {
          newsize = newsize + newsize
        } else {
          newsize = 5*newsize/4
        }
      }

      this.array = util.Arrays.copyOf(this.array, newsize)
    }
  }
}
