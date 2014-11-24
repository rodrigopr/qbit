package br.com.jusbrasil.qbit.index

import akka.actor._
import akka.pattern.gracefulStop
import akka.routing.ConsistentHashingPool
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.concurrent.duration._

class Indexer(numWorkers: Int) {
  private val logger = LoggerFactory.getLogger(classOf[Indexer])

  private val actorSystem = ActorSystem("index")

  private val indexWorkers = actorSystem.actorOf(
    ConsistentHashingPool(nrOfInstances = 10).props(Props[IndexActor].withMailbox("bounded-mailbox")),
    "workers-pool"
  )

  def send(task: IndexBatchTask) = {
    indexWorkers ! task
  }

  def shutdown(): Unit = {
    // try to stop the actor gracefully
    try {
      val stopped = gracefulStop(indexWorkers, 30.seconds)
      Await.result(stopped, 31.seconds)
    } catch {
      case ex: Exception => logger.error("Failure to shutdown gracefully: ", ex)
    } finally {
      actorSystem.shutdown()
    }
  }
}
