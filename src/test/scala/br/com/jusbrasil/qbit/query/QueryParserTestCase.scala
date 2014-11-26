package br.com.jusbrasil.qbit.query

import org.scalatest.{Matchers, FlatSpec}
import scala.util.Success

class QueryParserTestCase extends FlatSpec with Matchers {
  "Query parser" should "parse simple take-all statements" in {
    val source = """ RETURN aaa AND bbb """
    
    runParser(source) shouldBe
      Query(
        Return,
        And(
          IndexRef("aaa"),
          IndexRef("bbb")
        )
      )
  }

  it should "parse simple take-partial statements" in {
    val source = """ TAKE 50 FROM aaa AND bbb """
    
    runParser(source) shouldBe
      Query(
        Take(50),
        And(
          IndexRef("aaa"),
          IndexRef("bbb")
        )
      )
  }

  it should "parse simple count statements" in {
    val source = """ COUNT aaa AND bbb """
    
    runParser(source) shouldBe
      Query(
        Count,
        And(
          IndexRef("aaa"),
          IndexRef("bbb")
        )
      )
  }

  it should "parse queries with multiple operations" in {
    val source = """ RETURN aaa AND bbb AND ccc"""

    runParser(source) shouldBe
      Query(
        Return,
        And(
          And(IndexRef("aaa"), IndexRef("bbb")),
          IndexRef("ccc")
        )
      )
  }

  it should "parse queries respecting parenthesis in middle" in {
    val source = """ RETURN (aaa AND bbb) OR (ccc AND ddd)"""

    runParser(source) shouldBe
      Query(
        Return,
        Or(
          And(IndexRef("aaa"), IndexRef("bbb")),
          And(IndexRef("ccc"), IndexRef("ddd"))
        )
      )
  }

  it should "parse queries respecting parenthesis after" in {
    val source = """ RETURN aaa OR (bbb AND ccc)"""

    runParser(source) shouldBe
      Query(
        Return,
        Or(
          IndexRef("aaa"),
          And(
            IndexRef("bbb"),
            IndexRef("ccc")
          )
        )
      )
  }

  it should "parse queries respecting parenthesis before" in {
    val source = """ RETURN (aaa OR bbb) AND ccc"""

    runParser(source) shouldBe
      Query(
        Return,
        And(
          Or(
            IndexRef("aaa"),
            IndexRef("bbb")
          ),
          IndexRef("ccc")
        )
      )
  }

  it should "parse queries with index value" in {
    val source = """ RETURN aaa OR [1, -10, 2,1000,3, 99294967296] """

    runParser(source) shouldBe
      Query(
        Return,
        Or(
          IndexRef("aaa"),
          Index(1l, -10l, 2l, 1000l, 3l, 99294967296l)
        )
      )
  }

  it should "parse complex query" in {
    val source = """ TAKE 5 FROM ((aaa OR [1, -10, 2,1000,3, 99294967296]) AND NOT bbb) OR (ccc XOR ddd) """

    runParser(source) shouldBe
      Query(
        Take(5),
        Or(
          AndNot(
            Or(
              IndexRef("aaa"),
              Index(1l, -10l, 2l, 1000l, 3l, 99294967296l)
            ),
            IndexRef("bbb")
          ),
          Xor (
            IndexRef("ccc"),
            IndexRef("ddd")
          )
        )
      )
  }

  def runParser(query: String): Query = {
    val Success(result) = Parser(query)
    result
  }
}
