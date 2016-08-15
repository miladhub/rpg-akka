package akkarpg.game

import akka.actor.{Actor, ActorRef, Props}

object UserSession {
  def props(connectionHandler: ActorRef, game: ActorRef) = Props(new UserSession(connectionHandler, game))
  case class UserCommand(command: String)
  case class UserResponse(command: String)
  object Bye extends UserResponse("Bye!")
  object Welcome { def apply(who: String) = new Welcome(who) }
  class Welcome(who: String) extends UserResponse(s"Welcome, $who!")
  object AlreadyInGame extends UserResponse("Already in game.")
  object ImSorryWhat extends UserResponse("I'm sorry, what?")
  case class UserSessionEnded()
}

class UserSession(connectionHandler: ActorRef, game: ActorRef) extends Actor {
  var characterSession: ActorRef = _

  import CharacterSession._
  import Game._
  import UserSession._

  def receive = {
    case UserCommand(command: String) =>
      if (command.startsWith("enter as ")) {
        if (characterSession != null) {
          connectionHandler ! AlreadyInGame
        } else {
          val character = command.substring("enter as ".length).trim
          connectionHandler ! Welcome(character)
          characterSession = context.actorOf(CharacterSession.props(character, self, game))
          game ! CharacterAdded(character, characterSession)
        }
      } else if (command.startsWith("bye.")) {
        connectionHandler ! Bye
        if (characterSession != null)
          characterSession ! CharacterLeaving
        connectionHandler ! UserSessionEnded
      } else {
        Game.parse(command) match {
          case Some(msg) => game ! msg
          case None if characterSession != null => CharacterSession.parse(command) match {
            case Some(msg) => characterSession ! msg
            case None =>
          }
          case _ => connectionHandler ! ImSorryWhat
        }
      }
    case CharacterResponse(contents: String) =>
      connectionHandler ! UserResponse(contents)
    case GameResponse(contents: String) =>
      connectionHandler ! UserResponse(contents)
  }
}
