import akka.actor.{ActorSystem, Props}
import akka.io.Tcp.{Close, Received, Write}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import akka.util.ByteString
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration._

class AkkaTcpHelloSpec(_system: ActorSystem)
  extends TestKit(_system)
    with WordSpecLike
    with ImplicitSender
    with Matchers
    with BeforeAndAfterAll {

  def this() = this(ActorSystem("AkkaTcpHello"))

  override def afterAll: Unit = {
    Await.result(system.terminate(), 10.seconds)
  }

  "the client handler" should {
    "close the connection when saying bye" in {
      val handler = system.actorOf(UserSession.props(testActor))

      handler ! Received(ByteString("bye."))
      expectMsg(Write(ByteString("> Bye!\n")))
      expectMsg(Close)
    }

    "welcome characters as they enter" in {
      val handler = system.actorOf(UserSession.props(testActor))

      handler ! Received(ByteString("enter as John\n"))
      expectMsg(Write(ByteString("> Welcome, John!\n")))
    }

    "remember the character" in {
      val handler = system.actorOf(UserSession.props(testActor))

      handler ! Received(ByteString("enter as John\n"))
      expectMsg(Write(ByteString("> Welcome, John!\n")))

      handler ! Received(ByteString("who am i\n"))
      expectMsg(Write(ByteString("> Your name is John.\n")))
    }
  }
}
