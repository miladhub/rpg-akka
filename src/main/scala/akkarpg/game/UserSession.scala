package akkarpg.game

import akka.actor.{Actor, ActorRef, Props}

object UserSession {
  def props(connectionHandler: ActorRef, game: ActorRef) = Props(new UserSession(connectionHandler, game))
  case class UserCommand(command: String)
  case class UserResponse(command: String)
  case class UserSessionEnded()
}

class UserSession(connectionHandler: ActorRef, game: ActorRef) extends Actor {
  var characterSession: ActorRef = _

  import CharacterSession._
  import Game._
  import UserSession._

  def receive = {
    case UserCommand(command: String) =>
      if (command.startsWith("bye.")) {
        connectionHandler ! UserResponse("Bye!")
        if (characterSession != null)
          characterSession ! CharacterLeaving
        connectionHandler ! UserSessionEnded
      } else if (command.startsWith("enter as ")) {
        if (characterSession != null)
          throw new IllegalStateException("characterSession is not null")
        val character = command.substring("enter as ".length).trim
        connectionHandler ! UserResponse(s"Welcome, $character!")
        characterSession = context.actorOf(CharacterSession.props(character, self, game))
        game ! CharacterAdded(character, characterSession)
      } else {
        Game.parse(command) match {
          case Some(msg) => game ! msg
          case None if characterSession != null => CharacterSession.parse(command) match {
            case Some(msg) => characterSession ! msg
            case None => connectionHandler ! UserResponse("I'm sorry, what?")
          }
          case _ => connectionHandler ! UserResponse("I'm sorry, what?")
        }
      }
    case CharacterResponse(contents: String) =>
      connectionHandler ! UserResponse(contents)
    case GameResponse(contents: String) =>
      connectionHandler ! UserResponse(contents)
  }
}
