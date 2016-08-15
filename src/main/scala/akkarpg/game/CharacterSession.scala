package akkarpg.game

import akka.actor.{Actor, ActorRef, Props}

object CharacterSession {
  def props(character: String, userSession: ActorRef, game: ActorRef) =
    Props(new CharacterSession(character, userSession, game))

  def parseRequest(command: String) =
    if (command.startsWith("who am i"))
      Some(WhoAmI)
    else
      None

  sealed trait CharacterRequest
  case object WhoAmI extends CharacterRequest
  case object CharacterLeaving extends CharacterRequest

  case class CharacterResponse(contents: String)
  object YourNameIs { def apply(name: String) = new YourNameIs(name) }
  class YourNameIs(name: String) extends CharacterResponse(s"Your name is $name.")
}

class CharacterSession(character: String, userSession: ActorRef, game: ActorRef) extends Actor {
  import CharacterSession._
  import Game._

  def receive = {
    case WhoAmI =>
      userSession ! YourNameIs(character)
    case CharacterLeaving =>
      game ! CharacterRemoved(character)
  }
}
