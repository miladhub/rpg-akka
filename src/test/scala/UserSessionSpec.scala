import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
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
}
