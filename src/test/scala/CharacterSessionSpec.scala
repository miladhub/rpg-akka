import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import akkarpg.game.CharacterSession.{WhoAmI, YourNameIs}
import akkarpg.game.{CharacterSession, Game}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._

class CharacterSessionSpec(_system: ActorSystem)
  extends TestKit(_system)
    with WordSpecLike
    with ImplicitSender
    with Matchers
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("AkkaTcpRpg"))

  override def afterAll: Unit = {
    Await.result(system.terminate(), 10.seconds)
  }

  "the character session" should {
    "say who the character is" in {
      val game = system.actorOf(Props[Game])
      val charSession = system.actorOf(CharacterSession.props("John", testActor, game))

      charSession ! WhoAmI
      expectMsg(YourNameIs("John"))
    }
  }
}
