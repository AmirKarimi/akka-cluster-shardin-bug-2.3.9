package com.example

import akka.actor.{Props, ActorRef, ActorSystem}
import akka.cluster.Cluster
import akka.contrib.pattern.{ClusterSharding, ShardRegion}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits._

import Counter._

object ApplicationMain {
  implicit val timeout: Timeout = 5 seconds

  def main(args: Array[String]): Unit = {
    val ports = Seq("2551", "2552", "0")

    val portSystems = ports map { port =>
      val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=${port}").withFallback(ConfigFactory.load)
      val system = ActorSystem("MyActorSystem", config)

      ClusterSharding(system).start(
        typeName = Counter.shardName,
        entryProps = Some(Counter.props),
        idExtractor = Counter.idExtractor,
        shardResolver = Counter.shardResolver)

      (port, system)
    }

    val mainSystem = portSystems.find(_._1 == "0").map(_._2).get
    
    mainSystem.scheduler.scheduleOnce(3 seconds) {
      val counterRegion = ClusterSharding(mainSystem).shardRegion(Counter.shardName)
      val firstResult = Await.result(counterRegion ? Get("100"), timeout.duration)
      println(s"************ first result: ${firstResult} ***************")
      counterRegion ! EntryEnvelope("100", Increment)
      val secondResult = Await.result(counterRegion ? Get("100"), timeout.duration)
      println(s"************ second result: ${secondResult} ***************")

      portSystems.map(_._2).foreach(_.shutdown())
      mainSystem.awaitTermination()
    }

  }
}