package akkarpg.tcp

import java.net.InetSocketAddress

import akka.actor.{Actor, Props}
import akka.io.{IO, Tcp}
import akkarpg.game.Game
import com.typesafe.config.ConfigFactory

class AkkaTcpRpgServer extends Actor {
  import Tcp._
  import context.system

  val conf = ConfigFactory.load()
  val host = conf.getString("http.host")
  val port = conf.getInt("http.port")

  IO(Tcp) ! Bind(self, new InetSocketAddress(host, port))
  val game = system.actorOf(Props[Game])

  def receive = {
    case b@Bound(localAddress) =>
      println("Listening on address" + localAddress)

    case CommandFailed(_: Bind) => context stop self

    case c@Connected(remote, local) =>
      println("Client connected from " + remote)
      val connection = sender()
      val handler = context.actorOf(ConnectionHandler.props(connection, game))
      connection ! Register(handler)
  }
}
