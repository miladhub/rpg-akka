import akka.actor.{Actor, Props}

object CharacterSession {
  case class CharacterResponse(contents: String)
  case class WhoAmI()
  def props(character: String) = Props(new CharacterSession(character))
  def parse(command: String): Option[Any] =
    if (command.startsWith("who am i"))
      Some(WhoAmI)
    else
      None
}

class CharacterSession(character: String) extends Actor {
  import CharacterSession._

  def receive = {
    case WhoAmI => sender() ! CharacterResponse("Your name is " + character + ".")
  }
}
