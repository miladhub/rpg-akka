import java.net.InetSocketAddress

import CharacterSession.WhoAmI
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
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
      println("Listening on address" + localAddress)

    case CommandFailed(_: Bind) => context stop self

    case c@Connected(remote, local) =>
      println("Client connected from " + remote)
      val connection = sender()
      val handler = context.actorOf(UserSession.props(connection))
      connection ! Register(handler)
  }
}

object UserSession {
  case class CharacterMessage(contents: String)
  def props(connection: ActorRef) = Props(new UserSession(connection))
}

class UserSession(connection: ActorRef) extends Actor {
  var character: ActorRef = _

  import Tcp._
  import UserSession._

  def receive = {
    case Received(data) =>
      val command = data.decodeString("UTF-8")
      if (command.startsWith("bye.")) {
        connection ! Write(ByteString("> Bye!\n"))
        connection ! Close
      } else if (command.startsWith("enter as ")) {
        val character = command.substring("enter as ".length).trim
        connection ! Write(ByteString("> Welcome, " + character + "!\n"))
        this.character = context.actorOf(CharacterSession.props(character))
      } else if (command.startsWith("who am i")) {
        this.character ! WhoAmI
      } else {
        connection ! Write(ByteString("> I'm sorry, what?\n"))
      }
    case CharacterMessage(contents: String) =>
      connection ! Write(ByteString("> " + contents + "\n"))
    case PeerClosed =>
      println("Client disconnected")
      context stop self
  }
}

object CharacterSession {
  case class WhoAmI()
  def props(character: String) = Props(new CharacterSession(character))
}

class CharacterSession(character: String) extends Actor {
  import CharacterSession._
  import UserSession._

  def receive = {
    case WhoAmI => sender() ! CharacterMessage("Your name is " + character + ".")
  }
}