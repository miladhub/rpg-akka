import java.net.InetSocketAddress

import akka.actor.{Actor, ActorSystem, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString

object AkkaTcpHello extends App {
  val system = ActorSystem("AkkaTcpHello")
  val server = system.actorOf(Props[Server], "server")
}

class Server extends Actor {

  import Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 6789))

  def receive = {
    case b@Bound(localAddress) =>
      println("bound at address" + localAddress)

    case CommandFailed(_: Bind) => context stop self

    case c@Connected(remote, local) =>
      println("client connected from " + remote)
      val handler = context.actorOf(Props[SimplisticHandler])
      val connection = sender()
      connection ! Register(handler)
  }
}

class SimplisticHandler extends Actor {
  var character: String = _

  import Tcp._

  def receive = {
    case Received(data) =>
      val command = data.decodeString("UTF-8")
      if (command.startsWith("bye.")) {
        sender() ! Write(ByteString("> bye!\n"))
        sender() ! Close
      } else if (command.startsWith("enter as ")) {
        val character = command.substring("enter as ".length).trim
        sender() ! Write(ByteString("> welcome, " + character + "!\n"))
        this.character = character
      } else if (command.startsWith("who am i")) {
        sender() ! Write(ByteString("> your name is " + character + ".\n"))
        this.character = character
      } else {
        sender() ! Write(ByteString("> " + command))
      }
    case PeerClosed =>
      println("client disconnected")
      context stop self
  }
}