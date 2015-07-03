package com.example

import akka.actor.{Actor, PoisonPill, Props, ReceiveTimeout}
import akka.contrib.pattern.ShardRegion
import akka.persistence.PersistentActor
import scala.concurrent.duration._

class Counter extends Actor {
  import ShardRegion.Passivate
  import Counter._

  context.setReceiveTimeout(120.seconds)

  var count = 0

  def updateState(event: CounterChanged): Unit =
    count += event.delta

  override def receive: Receive = {
    case Increment      ⇒ updateState(CounterChanged(+1))
    case Decrement      ⇒ updateState(CounterChanged(-1))
    case Get(_)         ⇒ sender() ! count
    case ReceiveTimeout ⇒ context.parent ! Passivate(stopMessage = PoisonPill)
  }
}

object Counter {
  def props = Props(new Counter())

  val shardName = "Counter"

  val idExtractor: ShardRegion.IdExtractor = {
    case EntryEnvelope(id, payload) ⇒ (id.toString, payload)
    case msg @ Get(id)              ⇒ (id.toString, msg)
  }

  val shardResolver: ShardRegion.ShardResolver = msg ⇒ msg match {
    case EntryEnvelope(id, _) ⇒ (math.abs(id.hashCode) % 12).toString
    case Get(id)              ⇒ (math.abs(id.hashCode) % 12).toString
  }

  case class EntryEnvelope(id: String, payload: Any)
  case class Get(counterId: String)
  case object Increment
  case object Decrement
  case class CounterChanged(delta: Int)
}