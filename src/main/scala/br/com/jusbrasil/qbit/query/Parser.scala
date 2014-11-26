package br.com.jusbrasil.qbit.query

import scala.util.Try
import scala.util.parsing.combinator._


/**
 * Query parser for arbitrary binary operations
 */
object Parser extends JavaTokenParsers {
  def apply(source: String): Try[Query] = {
    parseAll(query, source.toLowerCase) match {
      case Success(r, _) ⇒ scala.util.Success(r)
      case Failure(msg, _) ⇒ scala.util.Failure(new RuntimeException(msg))
    }
  }

  // Main query parser
  private def query: Parser[Query] = (action ~ operation) ^^ { case action ~ ops ⇒ Query(action, ops) }


  // Actions
  private def action: Parser[QueryAction] = count | take | returnAll

  private def count: Parser[QueryAction] = "count" ^^^ Count

  private def take: Parser[QueryAction] = ("take" ~> intNumber <~ "from") ^^ { limit ⇒ Take(limit) }

  private def returnAll: Parser[QueryAction] = "return" ^^^ Return


  // Operations
  private def operation: Parser[Operation] = (predicate|parens) * (
      ("and" ~ "not") ^^^ { (a: Operation, b: Operation) => AndNot(a,b) } |
      "and" ^^^ { (a: Operation, b: Operation) => And(a,b) } |
      "or" ^^^ { (a: Operation, b: Operation) => Or(a,b) } |
      "xor" ^^^ { (a: Operation, b: Operation) => Xor(a,b) }
    )

  private def parens: Parser[Operation] = "(" ~> operation <~ ")"

  private def predicate: Parser[Operation] = index | indexRef


  // Index mapping
  private def index: Parser[Index] = "[" ~> ( repsep(longNumber, ",") ^^ { ls ⇒ Index(ls :_*) } ) <~ "]"
  
  private def indexRef: Parser[IndexRef] = ident ^^ { case n => IndexRef(n) }


  // Value mapping
  private def longNumber: Parser[Long] = wholeNumber ^^ { n => n.toLong }
  
  private def intNumber: Parser[Int] = wholeNumber ^^ { n => n.toInt }
}
