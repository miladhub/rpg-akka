import akkarpg.game.Game.HowManyUsers
import akkarpg.game.UserSession._
import akkarpg.game._
import org.scalatest._

class MessageParsersSpec extends FlatSpec with Matchers {
  it should "parse 'enter as'" in {
    UserSession.userRequest("enter as John") should be(EnterAs("John"))
  }

  it should "parse 'bye'" in {
    UserSession.userRequest("bye.") should be(Bye)
  }

  it should "parse 'who am i'" in {
    UserSession.userRequest("who am i") should be(WhoAmI)
  }

  it should "parse 'How many users are online?'" in {
    UserSession.userRequest("How many users are online?") should be(HowManyUsers)
  }

  it should "parse incorrect messages" in {
    UserSession.userRequest("foobar") should be(UnrecognizedRequest)
  }
}
