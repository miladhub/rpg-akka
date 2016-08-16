package akkarpg.game

import akka.actor.{Actor, ActorRef}

object Game {
  def parse(command: String) =
    if (command.startsWith("How many users are online?"))
      Some(HowManyUsers)
    else
      None

  sealed trait GameRequest
  case object HowManyUsers extends GameRequest
  case class AddCharacter(character: String) extends GameRequest
  case class RemoveCharacter(character: String) extends GameRequest

  case class GameResponse(contents: String)
}

class Game extends Actor {
  import Game._

  var characters = Set[String]()

  def receive = {
    case HowManyUsers =>
      val userSession: ActorRef = sender()
      userSession ! GameResponse(characters.size.toString)
    case AddCharacter(character: String) =>
      characters = characters + character
    case RemoveCharacter(character: String) =>
      characters = characters - character
  }
}
