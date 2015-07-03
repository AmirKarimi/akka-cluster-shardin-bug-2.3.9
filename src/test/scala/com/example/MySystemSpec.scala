package com.example

import akka.actor.{ActorRef, ActorSystem, Actor, Props}
import akka.contrib.pattern.ClusterSharding
import akka.testkit.{ TestActors, TestKit, ImplicitSender }
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import org.scalatest.BeforeAndAfterAll
 
class MySystemSpec(_system: ActorSystem) extends TestKit(_system) with ImplicitSender
  with WordSpecLike with Matchers with BeforeAndAfterAll {
 
  def this() = this(ActorSystem("MySpec"))
 
  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
 
  "Counter sharding region" must {
    "work as excepted" in {
      ClusterSharding(system).start(
        typeName = "Counter",
        entryProps = Some(Props[Counter]),
        idExtractor = Counter.idExtractor,
        shardResolver = Counter.shardResolver)

      val counterRegion: ActorRef = ClusterSharding(system).shardRegion("Counter")
      counterRegion ! Get(100)
      expectMsg(0)

      counterRegion ! EntryEnvelope(100, Increment)
      counterRegion ! Get(100)
      expectMsg(1)
    }
  }

}
