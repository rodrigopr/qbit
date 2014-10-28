package br.com.jusbrasil.qbit.bitmap

import br.com.jusbrasil.qbit.utils.ResizableArrayLong

private[bitmap] class FastBitmap(estimatedSize: Int) {
  private val indexes = new ResizableArrayLong(estimatedSize)
  private val values = new ResizableArrayLong(estimatedSize)

  private var currentIndex = Long.MinValue
  private var currentValue = 0l

  def fastAdd(index: Long, value: Long) {
    if(currentIndex < index) {
      saveCurrentAndMoveTo(index)
    }
    currentValue = value
  }

  def fastAnd(index: Long, value: Long) {
    if(currentIndex < index) {
      saveCurrentAndMoveTo(index)
    }
    currentValue &= value
  }

  def fastOr(index: Long, value: Long) {
    if(currentIndex < index) {
      saveCurrentAndMoveTo(index)
    }
    currentValue |= value
  }

  def fastXor(index: Long, value: Long) {
    if(currentIndex < index) {
      saveCurrentAndMoveTo(index)
    }
    currentValue ^= value
  }

  def saveCurrentAndMoveTo(newIndex: Long) {
    if(currentValue != 0l) {
      indexes.append(currentIndex)
      values.append(currentValue)

      currentValue = 0
    }

    currentIndex = newIndex
  }

  def finalBitmap() = {
    if(currentValue != 0l) {
      indexes.append(currentIndex)
      values.append(currentValue)
    }

    new SparseBitmap(indexes.innerArray, values.innerArray, values.size)
  }
}
