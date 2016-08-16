package akkarpg.game

import akka.actor.{Actor, ActorRef, Props}

object UserSession {
  def props(connectionHandler: ActorRef, game: ActorRef) = Props(new UserSession(connectionHandler, game))

  sealed trait UserRequest
  case object WhoAmI extends UserRequest
  case object UnrecognizedRequest extends UserRequest

  case class UserResponse(command: String)
  object Bye extends UserResponse("Bye!")
  object Welcome { def apply(who: String) = new Welcome(who) }
  class Welcome(who: String) extends UserResponse(s"Welcome, $who!")
  object AlreadyInGame extends UserResponse("Already in game.")
  object ImSorryWhat extends UserResponse("I'm sorry, what?")
  object YourNameIs { def apply(name: String) = new YourNameIs(name) }
  class YourNameIs(name: String) extends UserResponse(s"Your name is $name.")

  case object UserSessionEnded
  case class EnterAs(character: String)
  def userRequest(command: String) = {
    if (command.startsWith("enter as ")) {
      val character = command.substring("enter as ".length).trim
      EnterAs(character)
    } else if (command.startsWith("bye.")) {
      Bye
    } else if (command.startsWith("who am i")) {
      WhoAmI
    } else {
      Game.parse(command) match {
        case Some(msg) => msg
        case None => UnrecognizedRequest
      }
    }
  }
}

class UserSession(connectionHandler: ActorRef, game: ActorRef) extends Actor {
  var character: Option[String] = None

  import Game._
  import UserSession._

  def receive = {
    case EnterAs(character: String) =>
      if (inGame) {
        connectionHandler ! AlreadyInGame
      } else {
        this.character = Some(character)
        connectionHandler ! Welcome(character)
        game ! AddCharacter(character)
      }
    case Bye =>
      if (inGame)
        game ! RemoveCharacter(character.get)
      connectionHandler ! Bye
      connectionHandler ! UserSessionEnded
    case msg: GameRequest =>
      game ! msg
    case GameResponse(contents: String) =>
      connectionHandler ! UserResponse(contents)
    case WhoAmI =>
      connectionHandler ! YourNameIs(character.get)
    case _ =>
      connectionHandler ! ImSorryWhat
  }

  def inGame = {
    this.character.isDefined
  }
}
