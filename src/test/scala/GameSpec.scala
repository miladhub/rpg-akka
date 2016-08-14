import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import akkarpg.game.Game
import akkarpg.game.Game.{CharacterAdded, CharacterRemoved, GameResponse, HowManyUsers}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._

class GameSpec(_system: ActorSystem)
  extends TestKit(_system)
    with WordSpecLike
    with ImplicitSender
    with Matchers
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("AkkaTcpRpg"))

  override def afterAll: Unit = {
    Await.result(system.terminate(), 10.seconds)
  }

  "the game" should {
    "know how many characters are in" in {
      val john = TestProbe("john")
      val jim = TestProbe("jim")

      val game = system.actorOf(Props[Game])

      game ! CharacterAdded("john", john.testActor)
      game ! CharacterAdded("jim", jim.testActor)

      game ! HowManyUsers
      expectMsg(GameResponse("2"))
    }

    "remove users when they go away" in {
      val john = TestProbe("john")
      val jim = TestProbe("jim")

      val game = system.actorOf(Props[Game])

      game ! CharacterAdded("john", john.testActor)
      game ! CharacterAdded("jim", jim.testActor)

      game ! HowManyUsers
      expectMsg(GameResponse("2"))

      game ! CharacterRemoved("john")

      game ! HowManyUsers
      expectMsg(GameResponse("1"))
    }
  }
}
