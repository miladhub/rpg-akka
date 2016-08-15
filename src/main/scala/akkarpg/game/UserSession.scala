package akkarpg.game

import akka.actor.{Actor, ActorRef, Props}

object UserSession {
  def props(connectionHandler: ActorRef, game: ActorRef) = Props(new UserSession(connectionHandler, game))

  case class UserRequest(command: String)

  case class UserResponse(command: String)
  object Bye extends UserResponse("Bye!")
  object Welcome { def apply(who: String) = new Welcome(who) }
  class Welcome(who: String) extends UserResponse(s"Welcome, $who!")
  object AlreadyInGame extends UserResponse("Already in game.")
  object ImSorryWhat extends UserResponse("I'm sorry, what?")

  case class UserSessionEnded()
  case class EnterAs(character: String)
  def parseRequest(command: String) = {
    if (command.startsWith("enter as ")) {
      val character = command.substring("enter as ".length).trim
      EnterAs(character)
    } else if (command.startsWith("bye.")) {
      Bye
    } else {
      Game.parseRequest(command) match {
        case Some(msg) => msg
        case None => CharacterSession.parseRequest(command) match {
          case Some(msg) => msg
          case None =>
        }
      }
    }
  }
}

class UserSession(connectionHandler: ActorRef, game: ActorRef) extends Actor {
  var characterSession: ActorRef = _

  import CharacterSession._
  import Game._
  import UserSession._

  def receive = {
    case EnterAs(character: String) =>
      if (characterSession != null) {
        connectionHandler ! AlreadyInGame
      } else {
        connectionHandler ! Welcome(character)
        characterSession = context.actorOf(CharacterSession.props(character, self, game))
        game ! CharacterAdded(character, characterSession)
      }
    case Bye =>
      connectionHandler ! Bye
      if (characterSession != null)
        characterSession ! CharacterLeaving
      connectionHandler ! UserSessionEnded
    case msg: GameRequest =>
      game ! msg
    case msg: CharacterRequest =>
      characterSession ! msg
    case CharacterResponse(contents: String) =>
      connectionHandler ! UserResponse(contents)
    case GameResponse(contents: String) =>
      connectionHandler ! UserResponse(contents)
    case _ =>
      connectionHandler ! ImSorryWhat
  }
}
