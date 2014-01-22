package com.github.timblair.bluesnap

import org.scalatra._
import scala.slick.session.Database
import scala.slick.driver.PostgresDriver.simple._

import Database.threadLocalSession

trait SlickRoutes extends ScalatraServlet {

    val db: Database

    get("/db/create-tables") {
        db withTransaction {
            (Players.ddl ++ Games.ddl ++ Availabilities.ddl).create
        }
    }
}
