package akkarpg.game

import akka.actor.{Actor, ActorRef, Props}

object CharacterSession {
  case class CharacterResponse(contents: String)
  case class WhoAmI()
  case class CharacterLeaving()
  object YourNameIs { def apply(name: String) = new YourNameIs(name) }
  class YourNameIs(name: String) extends CharacterResponse(s"Your name is $name.")
  def props(character: String, userSession: ActorRef, game: ActorRef) =
    Props(new CharacterSession(character, userSession, game))
  def parse(command: String): Option[Any] =
    if (command.startsWith("who am i"))
      Some(WhoAmI)
    else
      None
}

class CharacterSession(character: String, userSession: ActorRef, game: ActorRef) extends Actor {
  import CharacterSession._
  import Game._

  def receive = {
    case WhoAmI => userSession ! YourNameIs(character)
    case CharacterLeaving => game ! CharacterRemoved(character)
  }
}
