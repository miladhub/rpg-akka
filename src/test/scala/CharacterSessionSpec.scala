import CharacterSession.{CharacterResponse, WhoAmI}
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
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
      val charSession = system.actorOf(CharacterSession.props("John", testActor))

      charSession ! WhoAmI
      expectMsg(CharacterResponse("Your name is John."))
    }
  }
}
