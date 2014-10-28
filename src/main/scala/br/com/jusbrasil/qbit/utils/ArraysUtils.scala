package br.com.jusbrasil.qbit.utils

import scala.reflect.ClassTag

case class SubArray[T](array: Array[T], from: Int = 0, to: Int = 1)

object ArraysUtils {
  def fastUnion[T : ClassTag](finalSize: Int, arrays: SubArray[T]*): Array[T] = {
    val finalArray = new Array[T](finalSize)

    arrays.foldLeft(0) { (acc, a) =>
      val length = a.to - a.from
      if(length > 0) {
        System.arraycopy(a.array, a.from, finalArray, acc, length)
      }

      acc + length
    }

    finalArray
  }
}
