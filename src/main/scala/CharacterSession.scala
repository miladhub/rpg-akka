import akka.actor.{Actor, Props}

object CharacterSession {
  case class WhoAmI()
  def props(character: String) = Props(new CharacterSession(character))
}

class CharacterSession(character: String) extends Actor {
  import CharacterSession._
  import UserSession._

  def receive = {
    case WhoAmI => sender() ! CharacterMessage("Your name is " + character + ".")
  }
}
