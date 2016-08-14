package akkarpg.main

import akka.actor.{ActorSystem, Props}
import akkarpg.tcp.AkkaTcpRpgServer

object AkkaTcpRpg extends App {
  val system = ActorSystem("AkkaTcpRpg")
  val server = system.actorOf(Props[AkkaTcpRpgServer], "AkkaTcpRpgServer")
}
