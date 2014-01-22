package com.github.timblair.bluesnap

import org.scalatra._
import scalate.ScalateSupport

// JSON-related libraries
import org.json4s.{DefaultFormats, Formats}

// JSON handling support from Scalatra
import org.scalatra.json._
import scala.slick.session.Database
import scala.collection.mutable. { Map, HashMap }
import scala.slick.driver.PostgresDriver.simple._
import Database.threadLocalSession
import java.sql.Timestamp

case class Player(id: Long, name: String)
case class Availability(status: String, game: Long, player: Long)
case class Game(id: Long, time: Timestamp, opponent: String, field: String)

case class MainServlet(db: Database) extends BluesnapStack with SlickRoutes with JacksonJsonSupport with ScalateSupport {
  // Sets up automatic case class to JSON output serialization, required by
  // the JValueResult trait.
  protected implicit val jsonFormats: Formats = DefaultFormats


  get("/gameIcs") {
    // TODO: download as a file
    db withTransaction {
      val game = Games.findById(params("game").toLong)
      val player = Players.findById(params("player").toLong)
      game match {
        case Some(g) => player match {
          case Some(p) => println(Event.generate(g, p))
          case _ => println("No Player found")
        }
        case _ => println("No Game found")
      }
    }
  }

  post("/availability") {
    println("availability called")
    db withTransaction {
      Availabilities.upsert(new Availability(params("state"), params("game").toLong, params("player").toLong))
    }
  }

  get("/player") {
    db withTransaction {
      Players.autoInc.insert(params("name"))
    }
  }

  post("/game") {
    db withTransaction {
      Games.autoInc.insert(new java.sql.Timestamp(params("time").toLong), params("opponent"), params("field"))
    }
  }

  get("/addGame") {
    ssp("/game")
  }

  get("/") {
    contentType="text/html"
    db withTransaction {
      val games = Games.findNext
      val players = Players.all
      val avails = Availabilities.forGames(games)
      ssp("/avail", "games" -> games, "players" -> players, "avails" -> byPlayer(avails), "gameAvailability" -> gameCounts(avails))
    }
  }
  
  def byPlayer(avails: List[Availability]) = {
      val map: Map[Long, List[(Long, String)]] = Map()
      for( a <- avails ) {
          val result: (Long, String) = (a.game, a.status);
          val l = map.getOrElse(a.player, Nil)
          map.put(a.player, result :: l)
      }
      map
  }

  def gameCounts(avails: List[Availability]) = {
    val map = new HashMap[Long, Long]().withDefaultValue(0)
    for(a <- avails if a.status == "yes") {
      map.put(a.game, map(a.game) + 1)
    }
    map
  }
}
