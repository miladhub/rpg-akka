package akkarpg.game

import akka.actor.{Actor, ActorRef}
import akkarpg.game.UserSession.WhoAmI

object Game {
  def parseRequest(command: String) =
    if (command.startsWith("How many users are online?"))
      Some(HowManyUsers)
    else if (command.startsWith("who am i"))
      Some(WhoAmI)
    else
      None

  sealed trait GameRequest
  case object HowManyUsers extends GameRequest
  case class CharacterAdded(character: String, userSession: ActorRef) extends GameRequest
  case class CharacterRemoved(character: String) extends GameRequest

  case class GameResponse(contents: String)
}

class Game extends Actor {
  import Game._

  val characters = collection.mutable.Map[String, ActorRef]()

  def receive = {
    case HowManyUsers =>
      sender() ! GameResponse(characters.size.toString)
    case CharacterAdded(character: String, userSession: ActorRef) =>
      characters += (character -> userSession)
    case CharacterRemoved(character: String) =>
      characters -= character
  }
}
