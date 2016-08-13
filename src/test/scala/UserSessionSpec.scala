import akka.actor.ActorSystem
import akka.io.Tcp.{Close, Received, Write}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.ByteString
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
    "close the connection when saying bye" in {
      val userSession = system.actorOf(UserSession.props(testActor))

      userSession ! Received(ByteString("bye."))
      expectMsg(Write(ByteString("> Bye!\n")))
      expectMsg(Close)
    }

    "welcome characters as they enter" in {
      val userSession = system.actorOf(UserSession.props(testActor))

      userSession ! Received(ByteString("enter as John\n"))
      expectMsg(Write(ByteString("> Welcome, John!\n")))
    }

    "say what to unknown commands" in {
      val userSession = system.actorOf(UserSession.props(testActor))

      userSession ! Received(ByteString("foobar\n"))
      expectMsg(Write(ByteString("> I'm sorry, what?\n")))
    }

    "remember the character" in {
      val userSession = system.actorOf(UserSession.props(testActor))

      userSession ! Received(ByteString("enter as John\n"))
      expectMsg(Write(ByteString("> Welcome, John!\n")))

      userSession ! Received(ByteString("who am i\n"))
      expectMsg(Write(ByteString("> Your name is John.\n")))
    }
  }
}
