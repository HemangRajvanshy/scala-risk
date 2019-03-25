package actors

import akka.actor.{ActorRef, Props}
import akka.testkit.TestProbe
import models.{Client, Player}
import org.scalatest.GivenWhenThen

import scala.util.Random
import scala.language.postfixOps

class GameActorSpec extends TestKitSpec with GivenWhenThen {

  val logger = play.api.Logger(getClass)
  val numClients = 4
  var probes = new Array[TestProbe](numClients)
  var clients = new Array[ClientWithActor](numClients)
  var players = new Array[Player](numClients)
  var tokens = new Array[String](numClients)
  var publicTokens = new Array[String](numClients)
  val names = new Array[String](numClients)

  var gameActor: Option[ActorRef] = None

  def initializeTest(): Unit = {
    for (i <- 0 until numClients) {
      // Create TestProbes
      probes(i) = TestProbe(s"client$i")

      // Generate tokens and publicTokens
      tokens(i) = (Random.alphanumeric take 16).mkString
      publicTokens(i) = (Random.alphanumeric take 16).mkString

      // Generate names
      names(i) = (Random.alphanumeric take 16).mkString

      // Create Clients
      clients(i) = ClientWithActor(Client(tokens(i), publicTokens(i), Some(names(i))), probes(i).ref)

      // Create Players
      players(i) = new Player(names(i), 0, Some(clients(i)))
    }
  }

  behavior of "GameActor"
  it should "be able to be constructed" in {
    initializeTest()
    gameActor = Some(system.actorOf(Props(new GameActor(players))))
  }
}
