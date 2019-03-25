package actors

import akka.actor.{ActorRef, Props}
import akka.testkit.TestProbe
import models._
import org.scalatest.GivenWhenThen

import scala.util.Random

class GameSetupActorSpec extends TestKitSpec with GivenWhenThen {
  val logger = play.api.Logger(getClass)
  val numClients = 4
  var probes = new Array[TestProbe](numClients)
  var clients = new Array[ClientWithActor](numClients)
  var players = new Array[Player](numClients)
  var tokens = new Array[String](numClients)
  var publicTokens = new Array[String](numClients)
  val names = new Array[String](numClients)
  var game: Option[Game] = None

  var gameSetupActor: Option[ActorRef] = None

  var currentTurn: String = ""

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
    // Create game
    game = Game(players) match {
      case Right(createdGame) =>
        logger.info("GOT GAME")
        Some(createdGame)
      case Left(errorMsg) =>
        logger.error(s"Error creating game: $errorMsg")
        throw new RuntimeException("Something went wrong lmao")
    }
  }

  behavior of "GameSetupActor"
  it should "be able to be constructed" in {
    initializeTest()
    gameSetupActor = Some(system.actorOf(GameSetupActor.props(players, game.get)))

    And("send each player a NotifyGameStarted and SendMapResource")
    probes foreach(probe => {
      probe.expectMsgAllClassOf(classOf[NotifyGameStarted], classOf[SendMapResource])
    })

    And("send each player a NotifyTurn")
    probes foreach(probe => {
      probe.expectMsgPF() {
        case NotifyTurn(publicToken) => currentTurn = publicToken
      }
      probe.expectNoMessage()
    })
  }

  it should "allow a player to place an army" in {
    val currentPlayer = publicTokens.indexOf(currentTurn)
    gameSetupActor.get ! PlaceArmy(tokens(currentPlayer), 0)

    Then("Everyone should get a NotifyGameState and a NotifyTurn")
    probes foreach(probe => {
      probe.expectMsgClass(classOf[NotifyGameState])
      probe.expectMsgPF() {
        case NotifyTurn(publicToken) => currentTurn = publicToken
      }
      probe.expectNoMessage()
    })
  }

}
