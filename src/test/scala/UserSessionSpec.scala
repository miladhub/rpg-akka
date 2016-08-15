import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import akkarpg.game.Game.{CharacterAdded, CharacterRemoved, HowManyUsers}
import akkarpg.game.UserSession._
import akkarpg.game.{Game, UserSession}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._

class UserSessionSpec(_system: ActorSystem)
  extends TestKit(_system)
    with WordSpecLike
    with ImplicitSender
    with Matchers
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("AkkaTcpRpg"))

  override def afterAll: Unit = {
    Await.result(system.terminate(), 10.seconds)
  }

  "the user session" should {
    "end when the user says bye" in {
      val game = system.actorOf(Props[Game])

      val userSession = system.actorOf(UserSession.props(testActor, game))

      userSession ! Bye
      expectMsg(Bye)
      expectMsg(UserSessionEnded)
    }

    "welcome characters as they enter" in {
      val game = system.actorOf(Props[Game])

      val userSession = system.actorOf(UserSession.props(testActor, game))

      userSession ! EnterAs("John")
      expectMsg(Welcome("John"))
    }

    "disallow entering twice" in {
      val game = system.actorOf(Props[Game])

      val userSession = system.actorOf(UserSession.props(testActor, game))

      userSession ! EnterAs("John")
      expectMsg(Welcome("John"))

      userSession ! EnterAs("John")
      expectMsg(AlreadyInGame)
    }

    "say what to unknown commands" in {
      val game = system.actorOf(Props[Game])

      val userSession = system.actorOf(UserSession.props(testActor, game))

      userSession ! "foobar"
      expectMsg(ImSorryWhat)
    }

    "say who the character is" in {
      val game = system.actorOf(Props[Game])

      val userSession = system.actorOf(UserSession.props(testActor, game))

      userSession ! EnterAs("John")
      expectMsg(Welcome("John"))

      userSession ! WhoAmI
      expectMsg(YourNameIs("John"))
    }
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
  }
}
