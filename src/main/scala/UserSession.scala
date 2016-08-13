import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp
import akka.util.ByteString

object UserSession {
  case class CharacterMessage(contents: String)
  def props(connection: ActorRef) = Props(new UserSession(connection))
}

class UserSession(connection: ActorRef) extends Actor {
  var character: ActorRef = _

  import CharacterSession._
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
