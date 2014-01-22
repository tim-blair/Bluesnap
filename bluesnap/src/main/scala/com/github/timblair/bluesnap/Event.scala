package com.github.timblair.bluesnap

import java.sql.Timestamp
// TODO: import less stuff, include ical4j in here

import net.fortuna.ical4j.data._
import net.fortuna.ical4j.filter._
import net.fortuna.ical4j.model._
import net.fortuna.ical4j.model.component._
import net.fortuna.ical4j.model.parameter._
import net.fortuna.ical4j.model.property._
import net.fortuna.ical4j.transform._
import net.fortuna.ical4j.util._


object Event {
  def generate(game: Game, player: Player): String = {
    // Create a TimeZone
    val registry = TimeZoneRegistryFactory.getInstance().createRegistry()
    val timezone = registry.getTimeZone("America/Toronto")
    val tz = timezone.getVTimeZone()

    // Create the event
    val eventName = "Game vs " + game.opponent
    val startDate = new DateTime(game.time.getTime())
    val endDate = new DateTime(game.time.getTime() + 60 * 60 * 1000)
    val meeting = new VEvent(startDate, endDate, eventName)

    // add timezone info..
    meeting.getProperties().add(tz.getTimeZoneId())

    // generate unique identifier..
    val ug = new UidGenerator("uidGen")
    val uid = ug.generateUid()
    meeting.getProperties().add(uid)

    // add attendees..
    val user = new Attendee() //URI.create("mailto:dev1@mycompany.com"))
    user.getParameters().add(Role.REQ_PARTICIPANT)
    user.getParameters().add(new Cn(player.name))
    meeting.getProperties().add(user)

    // Create a calendar
    val icsCalendar = new Calendar()
    icsCalendar.getProperties().add(new ProdId("-//Events Calendar//iCal4j 1.0//EN"))
    icsCalendar.getProperties().add(CalScale.GREGORIAN)

    // Add the event and print
    icsCalendar.getComponents().add(meeting)
    icsCalendar.toString()
  }
}
