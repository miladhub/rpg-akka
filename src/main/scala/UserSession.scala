import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp
import akka.util.ByteString

object UserSession {
  def props(connection: ActorRef) = Props(new UserSession(connection))
}

class UserSession(connection: ActorRef) extends Actor {
  var characterSession: ActorRef = _

  import Tcp._
  import CharacterSession._

  def receive = {
    case Received(data) =>
      val command = data.decodeString("UTF-8")
      if (command.startsWith("bye.")) {
        connection ! Write(ByteString("> Bye!\n"))
        connection ! Close
      } else if (command.startsWith("enter as ")) {
        val character = command.substring("enter as ".length).trim
        connection ! Write(ByteString("> Welcome, " + character + "!\n"))
        this.characterSession = context.actorOf(CharacterSession.props(character, self))
      } else {
        CharacterSession.parse(command) match {
          case Some(msg) => this.characterSession ! msg
          case None => connection ! Write(ByteString("> I'm sorry, what?\n"))
        }
      }
    case CharacterResponse(contents: String) =>
      connection ! Write(ByteString("> " + contents + "\n"))
    case PeerClosed =>
      println("Client disconnected")
      context stop self
  }
}
