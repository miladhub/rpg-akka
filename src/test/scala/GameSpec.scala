import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import akkarpg.game.Game.{CharacterAdded, CharacterRemoved, GameResponse, HowManyUsers}
import akkarpg.game.UserSession._
import akkarpg.game.{Game, UserSession}
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
      val johnSession = system.actorOf(UserSession.props(john.testActor, game))
      val jimSession = system.actorOf(UserSession.props(jim.testActor, game))

      johnSession ! EnterAs("John")
      jimSession ! EnterAs("Jim")
      johnSession ! HowManyUsers

      john.expectMsg(Welcome("John"))
      john.expectMsg(UserResponse("2"))
    }

    "remove users when they go away" in {
      val john = TestProbe("john")
      val game = TestProbe("game")
      val johnSession = system.actorOf(UserSession.props(john.testActor, game.testActor))

      johnSession ! EnterAs("John")
      johnSession ! Bye

      game.expectMsg(CharacterAdded("John"))
      game.expectMsg(CharacterRemoved("John"))
    }

    "keep track of users" in {
      val game = system.actorOf(Props[Game])

      game ! CharacterAdded("John")
      game ! CharacterAdded("Jim")
      game ! CharacterRemoved("John")
      game ! HowManyUsers

      expectMsg(GameResponse("1"))
    }
  }
}
