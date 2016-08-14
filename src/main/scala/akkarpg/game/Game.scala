package akkarpg.game

import akka.actor.{Actor, ActorRef}

object Game {
  def parse(command: String) =
    if (command.startsWith("How many users are online?"))
      Some(HowManyUsers)
    else
      None
  case class GameResponse(contents: String)
  case class HowManyUsers()
  case class CharacterAdded(character: String, characterSession: ActorRef)
  case class CharacterRemoved(character: String)
}

class Game extends Actor {
  import Game._

  val characters = collection.mutable.Map[String, ActorRef]()

  def receive = {
    case HowManyUsers =>
      sender() ! GameResponse(characters.size.toString)
    case CharacterAdded(character: String, characterSession: ActorRef) =>
      characters += (character -> characterSession)
    case CharacterRemoved(character: String) =>
      characters -= character
  }
}
