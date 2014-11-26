package br.com.jusbrasil.qbit

package object query {

  /**
   * User query
   *
   * @param action - Action to be performance on the result of the operations
   * @param ops - operations this query want to execute
   */
  case class Query(action: QueryAction, ops: Operation*)


  /**
   * Mapping for user action on an bitmap
   */
  sealed trait QueryAction

  /**
   * Will return only the length of the final bitmap
   */
  object Count extends QueryAction

  /**
   * Will return all values in the bitmap
   */
  object ReturnAll extends QueryAction

  /**
   * Will return the result only partially,
   * the values and order will be chosen randomly
   *
   * @param limit - maximum of values to return
   */
  case class ReturnPartial(limit: Int) extends QueryAction


  /**
   * Base operation mapping
   */
  sealed trait Operation

  /**
   * Creates a new bitmap to be used on this operation
   *
   * @param values - values to populate new bitmap
   */
  case class Index(values: Long*) extends Operation

  /**
   * Reference to an index,
   * this operation shall use the bitmap from the datastore
   *
   * @param indexName - name of the index to be used, case insensitive
   */
  case class IndexRef(indexName: String) extends Operation


  case class And(l: Operation, rClause: Operation) extends Operation
  case class Or(lClause: Operation, rClause: Operation) extends Operation
  case class Xor(lClause: Operation, rClause: Operation) extends Operation
  case class AndNot(lClause: Operation, rClause: Operation) extends Operation
}
