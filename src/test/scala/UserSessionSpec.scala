import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import akkarpg.game.UserSession.{UserCommand, UserResponse, UserSessionEnded}
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

      userSession ! UserCommand("bye.")
      expectMsg(UserResponse("Bye!"))
      expectMsg(UserSessionEnded)
    }

    "welcome characters as they enter" in {
      val game = system.actorOf(Props[Game])

      val userSession = system.actorOf(UserSession.props(testActor, game))

      userSession ! UserCommand("enter as John")
      expectMsg(UserResponse("Welcome, John!"))
    }

    "say what to unknown commands" in {
      val game = system.actorOf(Props[Game])

      val userSession = system.actorOf(UserSession.props(testActor, game))

      userSession ! UserCommand("foobar")
      expectMsg(UserResponse("I'm sorry, what?"))
    }

    "remember the character" in {
      val game = system.actorOf(Props[Game])

      val userSession = system.actorOf(UserSession.props(testActor, game))

      userSession ! UserCommand("enter as John")
      expectMsg(UserResponse("Welcome, John!"))

      userSession ! UserCommand("who am i")
      expectMsg(UserResponse("Your name is John."))
    }
  }
}
