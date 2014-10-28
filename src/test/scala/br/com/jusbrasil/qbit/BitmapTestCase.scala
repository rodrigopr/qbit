package br.com.jusbrasil.qbit

import br.com.jusbrasil.qbit.bitmap._
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class BitmapTestCase extends FlatSpec with ShouldMatchers {
  "BitMap" should "set and get bits" in {
    var bitmap = SparseBitmap()

    bitmap.get(1) should be (false)
    bitmap.get(2) should be (false)
    bitmap.get(3) should be (false)
    bitmap.get(31) should be (false)
    bitmap.get(32) should be (false)
    bitmap.get(33) should be (false)
    bitmap.get(1231231231321123l) should be (false)
    bitmap.iterator.toList should be (Nil)

    bitmap = bitmap.set(1)
    bitmap.get(1) should be (true)
    bitmap.get(2) should be (false)
    bitmap.get(3) should be (false)
    bitmap.get(31) should be (false)
    bitmap.get(32) should be (false)
    bitmap.get(33) should be (false)
    bitmap.get(1231231231321123l) should be (false)
    bitmap.iterator.toList should be (List(1))

    bitmap = bitmap.set(2)
    bitmap.get(1) should be (true)
    bitmap.get(2) should be (true)
    bitmap.get(3) should be (false)
    bitmap.get(31) should be (false)
    bitmap.get(32) should be (false)
    bitmap.get(33) should be (false)
    bitmap.get(1231231231321123l) should be (false)
    bitmap.iterator.toList should be (List(1, 2))

    bitmap = bitmap.set(3)
    bitmap.get(1) should be (true)
    bitmap.get(2) should be (true)
    bitmap.get(3) should be (true)
    bitmap.get(31) should be (false)
    bitmap.get(32) should be (false)
    bitmap.get(33) should be (false)
    bitmap.get(1231231231321123l) should be (false)
    bitmap.iterator.toList should be (List(1, 2, 3))

    bitmap = bitmap.set(31)
    bitmap.get(1) should be (true)
    bitmap.get(2) should be (true)
    bitmap.get(3) should be (true)
    bitmap.get(31) should be (true)
    bitmap.get(32) should be (false)
    bitmap.get(33) should be (false)
    bitmap.get(1231231231321123l) should be (false)
    bitmap.iterator.toList should be (List(1, 2, 3, 31))

    bitmap = bitmap.set(1231231231321123l)
    bitmap.get(1) should be (true)
    bitmap.get(2) should be (true)
    bitmap.get(3) should be (true)
    bitmap.get(31) should be (true)
    bitmap.get(32) should be (false)
    bitmap.get(33) should be (false)
    bitmap.get(1231231231321123l) should be (true)
    bitmap.iterator.toList should be (List(1, 2, 3, 31, 1231231231321123l))

    bitmap = bitmap.set(33)
    bitmap.get(1) should be (true)
    bitmap.get(2) should be (true)
    bitmap.get(3) should be (true)
    bitmap.get(31) should be (true)
    bitmap.get(32) should be (false)
    bitmap.get(33) should be (true)
    bitmap.get(1231231231321123l) should be (true)
    bitmap.iterator.toList should be (List(1, 2, 3, 31, 33, 1231231231321123l))

    bitmap = bitmap.set(32)
    bitmap.get(1) should be (true)
    bitmap.get(2) should be (true)
    bitmap.get(3) should be (true)
    bitmap.get(31) should be (true)
    bitmap.get(32) should be (true)
    bitmap.get(33) should be (true)
    bitmap.get(1231231231321123l) should be (true)
    bitmap.iterator.toList should be (List(1, 2, 3, 31, 32, 33, 1231231231321123l))
  }

  "BitMap" should "support and" in {
    val a = SparseBitmap(Array[Long](1, 2, 3, 31, 32, 33, 1000))
    val b = SparseBitmap(Array[Long](3, 31, 32, 33, 9999))
    val and = BitmapOperator.and(Array(a, b))
    and.iterator.toList should be (List(3, 31, 32, 33))

    val and2 = BitmapOperator.and(Array(and, BitmapOperator.and(Array(a, b))))
    and2.iterator.toList should be (and.iterator.toList)

    val c = SparseBitmap(Array[Long](1, 2, 31, 33, 9999))
    val and3 = BitmapOperator.and(Array(a, b, c))
    and3.iterator.toList should be (List(31, 33))
  }

  "BitMap" should "support or" in {
    val a = SparseBitmap(Array[Long](1, 2, 3, 31, 32, 33, 1000))
    val b = SparseBitmap(Array[Long](3, 31, 32, 33, 9999))
    val or = BitmapOperator.or(Array(a, b))
    or.iterator.toList should be (List(1, 2, 3, 31, 32, 33, 1000, 9999))

    val or2 = BitmapOperator.or(Array(or, BitmapOperator.or(Array(a, b))))
    or2.iterator.toList should be (or.iterator.toList)

    val c = SparseBitmap(Array[Long](1, 2, 31, 33, 9999, 10000))
    val or3 = BitmapOperator.or(Array(a, b, c))
    or3.iterator.toList should be (List(1, 2, 3, 31, 32, 33, 1000, 9999, 10000))
  }

  "BitMap" should "support xor" in {
    val a = SparseBitmap(Array[Long](1, 2, 3, 31, 32, 33, 1000))
    val b = SparseBitmap(Array[Long](3, 31, 32, 33, 9999))
    val xor = BitmapOperator.xor(Array(a, b))
    xor.iterator.toList should be (List(1, 2, 1000, 9999))

    val xor2 = BitmapOperator.xor(Array(BitmapOperator.xor(Array(a, b)), a))
    xor2.iterator.toList should be (b.iterator.toList)
  }

  "BitMap" should "support cardinality count" in {
    val a = SparseBitmap(Array[Long](1, 2, 3, 31, 32, 33, 1000))
    val b = SparseBitmap(Array[Long](3, 31, 32, 33, 9999))
    val c = SparseBitmap(Array[Long](1, 2, 33, 9999, 10000))
    a.cardinality should be (7)
    b.cardinality should be (5)
    c.cardinality should be (5)
  }
}

