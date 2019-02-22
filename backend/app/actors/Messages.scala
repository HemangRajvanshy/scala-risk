package actors

import akka.actor.ActorRef
import models.{Client, GameState, RoomBrief, RoomStatus}
import play.api.libs.json.Json
import play.api.mvc.WebSocket.MessageFlowTransformer

// Messages for which actor
sealed trait RootMsg

sealed trait GameMsg {
  val token: String
  val gameId: String
}

// Messages that are sent to the client
sealed trait OutEvent

case class NotifyClientsChanged(strings: Seq[String]) extends OutEvent

case class NotifyRoomsChanged(rooms: Seq[RoomBrief]) extends OutEvent

case class NotifyRoomStatus(roomStatus: RoomStatus) extends OutEvent

case class Ok(msg: String) extends OutEvent

case class Err(msg: String) extends OutEvent

case class Ping(msg: String) extends OutEvent

case class Kill(msg: String) extends OutEvent


case class NotifyGameStarted(state: GameState) extends OutEvent

case class NotifyGameState(state: GameState) extends OutEvent


// Messages which are read (including sent from ourself to ourself
sealed trait InEvent

// Messages which are sent from the client, and can be deserialized
sealed trait SerializableInEvent extends InEvent

// Client first connected, give client token for identification
case class RegisterClient(client: Client, actor: ActorRef) extends InEvent with RootMsg

// Client first connected, give client token for identification
case class KeepAliveTick() extends InEvent with RootMsg

// Client response to our ping
case class Pong(token: String) extends SerializableInEvent with RootMsg

// Client tries to assign name
case class AssignName(name: String, token: String) extends SerializableInEvent with RootMsg

// Client tries to create room
case class CreateRoom(roomName: String, token: String) extends SerializableInEvent with RootMsg

// Client tries to join room
case class JoinRoom(roomId: String, token: String) extends SerializableInEvent with RootMsg

// Client marks himself ready
case class ClientReady(roomId: String, token: String) extends SerializableInEvent with RootMsg

case class StartGame(roomId: String, token: String) extends SerializableInEvent with RootMsg

case class TestGameMsg(gameId: String, token: String) extends SerializableInEvent with GameMsg

object SerializableInEvent {
  implicit val assignNameRead = Json.reads[AssignName]
  implicit val joinRoomRead = Json.reads[JoinRoom]
  implicit val createRoomRead = Json.reads[CreateRoom]
  implicit val readyRead = Json.reads[ClientReady]
  implicit val startGameRead = Json.reads[StartGame]
  implicit val pongRead = Json.reads[Pong]

  implicit val testGameMsgRead = Json.reads[TestGameMsg]
  implicit val serializableInEventRead = Json.reads[SerializableInEvent]
}

object OutEvent {
  implicit val notifyClientsChangedWrite = Json.writes[NotifyClientsChanged]
  implicit val notifyRoomsChangedWrite = Json.writes[NotifyRoomsChanged]
  implicit val notifyRoomStatusWrite = Json.writes[NotifyRoomStatus]
  implicit val okWrite = Json.writes[Ok]
  implicit val pingWrite = Json.writes[Ping]
  implicit val errWrite = Json.writes[Err]
  implicit val killWrite = Json.writes[Kill]

  implicit val notifyGameStateWrite = Json.writes[NotifyGameState]
  implicit val notifyGameStartedWrite = Json.writes[NotifyGameStarted]

  implicit val outEventFormat = Json.writes[OutEvent]
  implicit val messageFlowTransformer = MessageFlowTransformer.jsonMessageFlowTransformer[SerializableInEvent, OutEvent]
}

