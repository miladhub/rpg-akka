package akkarpg.game

import akka.actor.{Actor, ActorRef}

object Game {
  def gameRequest(command: String) =
    if (command.startsWith("How many users are online?"))
      Some(HowManyUsers)
    else
      None

  sealed trait GameRequest
  case object HowManyUsers extends GameRequest
  case class CharacterAdded(character: String) extends GameRequest
  case class CharacterRemoved(character: String) extends GameRequest

  case class GameResponse(contents: String)
}

class Game extends Actor {
  import Game._

  var characters = Set[String]()

  def receive = {
    case HowManyUsers =>
      val userSession: ActorRef = sender()
      userSession ! GameResponse(characters.size.toString)
    case CharacterAdded(character: String) =>
      characters = characters + character
    case CharacterRemoved(character: String) =>
      characters = characters - character
  }
}
