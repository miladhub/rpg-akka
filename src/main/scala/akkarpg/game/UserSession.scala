package akkarpg.game

import akka.actor.{Actor, ActorRef, Props}

object UserSession {
  def props(connectionHandler: ActorRef, game: ActorRef) = Props(new UserSession(connectionHandler, game))

  sealed trait UserRequest
  case object WhoAmI extends UserRequest

  case class UserResponse(command: String)
  object Bye extends UserResponse("Bye!")
  object Welcome { def apply(who: String) = new Welcome(who) }
  class Welcome(who: String) extends UserResponse(s"Welcome, $who!")
  object AlreadyInGame extends UserResponse("Already in game.")
  object ImSorryWhat extends UserResponse("I'm sorry, what?")
  object YourNameIs { def apply(name: String) = new YourNameIs(name) }
  class YourNameIs(name: String) extends UserResponse(s"Your name is $name.")

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
        case None =>
      }
    }
  }
}

class UserSession(connectionHandler: ActorRef, game: ActorRef) extends Actor {
  var character: String = _

  import Game._
  import UserSession._

  def receive = {
    case EnterAs(character: String) =>
      if (this.character != null) {
        connectionHandler ! AlreadyInGame
      } else {
        this.character = character
        connectionHandler ! Welcome(character)
        game ! CharacterAdded(character, self)
      }
    case Bye =>
      connectionHandler ! Bye
      if (character != null)
        game ! CharacterRemoved(character)
      connectionHandler ! UserSessionEnded
    case msg: GameRequest =>
      game ! msg
    case GameResponse(contents: String) =>
      connectionHandler ! UserResponse(contents)
    case WhoAmI =>
      connectionHandler ! YourNameIs(character)
    case _ =>
      connectionHandler ! ImSorryWhat
  }
}
