package akkarpg.tcp

import akka.actor.{Actor, ActorRef, Props}
import akkarpg.game.UserSession
import akkarpg.game.UserSession.{UserRequest, UserResponse, UserSessionEnded}

object ConnectionHandler {
  def props(connection: ActorRef, game: ActorRef) = Props(new ConnectionHandler(connection, game))
}

class ConnectionHandler(connection: ActorRef, game: ActorRef) extends Actor {
  val userSession = context.actorOf(UserSession.props(self, game))

  import akka.io.Tcp._
  import akka.util.ByteString

  def receive = {
    case Received(data) =>
      val command = data.decodeString("UTF-8")
      userSession ! UserSession.parseRequest(command)
    case UserResponse(contents: String) =>
      connection ! Write(ByteString("> " + contents + "\n"))
    case UserSessionEnded =>
      connection ! Close
    case PeerClosed =>
      println("Client disconnected")
      context stop self
  }
}
