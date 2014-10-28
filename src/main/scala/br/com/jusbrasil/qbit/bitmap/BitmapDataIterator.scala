package br.com.jusbrasil.qbit.bitmap

private[bitmap] class BitmapDataIterator(
  indexes: Array[Long],
  values: Array[Long],
  numBuckets: Int
) extends Comparable[BitmapDataIterator] {

  private var i = 0
  var currentIdx: Long = indexes(i)
  var currentValue: Long = values(i)

  def hasNext = i != numBuckets

  def moveForward() {
    i += 1

    if(hasNext) {
      currentIdx = indexes(i)
      currentValue = values(i)
    }
  }

  def moveTo(idx: Long) {
    while(hasNext && currentIdx < idx) { moveForward() }
  }

  def compareTo(o2: BitmapDataIterator): Int =
    if(currentIdx < o2.currentIdx) {
      -1
    } else if(currentIdx == o2.currentIdx) {
      0
    } else {
      1
    }

  override def toString = "DataIterator{i: %d, hn: %s, idx: %s}".format(i, hasNext, String.valueOf(currentIdx))
}
