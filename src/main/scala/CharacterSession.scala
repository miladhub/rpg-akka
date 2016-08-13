import akka.actor.{Actor, ActorRef, Props}

object CharacterSession {
  case class CharacterResponse(contents: String)
  case class WhoAmI()
  def props(character: String, userSession: ActorRef) = Props(new CharacterSession(character, userSession))
  def parse(command: String): Option[Any] =
    if (command.startsWith("who am i"))
      Some(WhoAmI)
    else
      None
}

class CharacterSession(character: String, userSession: ActorRef) extends Actor {
  import CharacterSession._

  def receive = {
    case WhoAmI => userSession ! CharacterResponse(s"Your name is $character.")
  }
}
