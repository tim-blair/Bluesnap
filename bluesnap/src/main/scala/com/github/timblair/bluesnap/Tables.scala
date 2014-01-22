package com.github.timblair.bluesnap

import java.sql.Timestamp
import scala.slick.driver.PostgresDriver.simple._
import Database.threadLocalSession

import scala.slick.session.Database
import scala.slick.jdbc.{GetResult, StaticQuery => Q}

object Players extends Table[Player]("players") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")

    def * = id ~ name <> (Player, Player.unapply _)
    def autoInc = name returning id
    def all =
      (for (p <- Query(Players)) yield p).list
    def allNames = 
      for (p <- Query(Players)) yield p.name
    def findById(pk: Long): Option[Player] =
      (for (p <- Query(Players) if p.id === pk) yield p).firstOption
}

object Games extends Table[Game]("games") {
    private val oneHour = 1000 * 60 * 60
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def time = column[Timestamp]("time")
    def opponent = column[String]("opponent")
    def field = column[String]("field")

    def * = id ~ time ~ opponent ~ field <> (Game, Game.unapply _)
    def autoInc = time ~ opponent ~ field returning id
    def findById(pk: Long): Option[Game] =
        (for (g <- Query(Games) if g.id === pk) yield g).firstOption
    // TODO: sort these by time
    def findNext: List[Game] = {
        val next = for (g <- Query(Games) if g.time > new Timestamp(System.currentTimeMillis() - oneHour)) yield g
        next.take(4).list
    }
}

object Availabilities extends Table[Availability]("availability") {
    def status = column[String]("status")
    def game = column[Long]("game")
    def player = column[Long]("player")

    def * = status ~ game ~ player <> (Availability, Availability.unapply _)

    def gameFk = foreignKey("game_fk", game, Games)(_.id)
    def playerFk = foreignKey("player_fk", player, Players)(_.id)

    def forGames(games: List[Game]) = {
        val ids = games.map(g => g.id)
        val avails = for (a <- Query(Availabilities) if a.game inSetBind ids) yield a
        avails.list
    }

    def upsert(avail: Availability) = {
        // Slick and Postgres both don't support upsert, so do this hacky thing instead
        (for (a <- Query(Availabilities) if a.game === avail.game && a.player === avail.player) yield a).delete
        Availabilities.insert(avail)
    }
}
