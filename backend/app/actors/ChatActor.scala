package actors

import java.time.LocalDateTime

import akka.actor.{Actor, Props}
import models.{Game, Room}

import scala.collection.mutable.HashMap

object ChatActor {
  def props(clients: HashMap[String, ClientWithActor], rooms: HashMap[String, Room]): Props =
    Props(new ChatActor(clients, rooms))
}

class ChatActor(clients: HashMap[String, ClientWithActor], rooms: HashMap[String, Room]) extends Actor {
  val logger = play.api.Logger(getClass)

  override def receive: Receive = {
    // TODO: Not O(n) search
    case MessageToUser(token, publicToken, message) => handleMessageToUser(token, publicToken, message)
    case MessageToRoom(token, roomId, message) => handleMessageToRoom(token, roomId, message)
  }

  def handleMessageToUser(token: String, publicToken: String, message: String): Unit = {
    clients.get(token) match {
      case Some(client) =>
        clients.find(_._2.client.publicToken == publicToken) match {
          case Some((_, recipient)) =>
            val time = LocalDateTime.now
            recipient.actor ! UserMessage(client.client.name getOrElse "", client.client.publicToken, message, time.toString)
            client.actor ! UserMessage(client.client.name getOrElse "", client.client.publicToken, message, time.toString)
          case None => sender() ! Err("Invalid recipient.")
        }
      case None => sender() ! Err("Invalid Token.")
    }
  }

  def handleMessageToRoom(token: String, roomId: String, message: String): Unit = {
    clients.get(token) match {
      case Some(sender) =>
        rooms.get(roomId) match {
          case Some(room) =>
            val time = LocalDateTime.now
            room.clients.values foreach (client => {
              client.actor ! RoomMessage(sender.client.name getOrElse "", message, time.toString)
            })
          case None => sender.actor ! Err("Invalid room.")
        }
      case None => sender() ! Err("Invalid recipient.")
    }
  }
}
