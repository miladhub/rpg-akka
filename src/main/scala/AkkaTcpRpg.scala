import java.net.InetSocketAddress

import akka.actor.{Actor, ActorSystem, Props}
import akka.io.{IO, Tcp}

object AkkaTcpRpg extends App {
  val system = ActorSystem("AkkaTcpRpg")
  val server = system.actorOf(Props[AkkaTcpRpgServer], "AkkaTcpRpgServer")
}

class AkkaTcpRpgServer extends Actor {
  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 6789))

  def receive = {
    case b@Bound(localAddress) =>
      println("Listening on address" + localAddress)

    case CommandFailed(_: Bind) => context stop self

    case c@Connected(remote, local) =>
      println("Client connected from " + remote)
      val connection = sender()
      val handler = context.actorOf(UserSession.props(connection))
      connection ! Register(handler)
  }
}
