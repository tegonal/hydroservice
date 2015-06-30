package models

import xml._
import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.mutable.MapBuilder
import play.api._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import java.text.NumberFormat
import java.util.Locale
import java.text.ParseException

object MeasurementType {
  val FLOW_M_3_S = "FLOW_M3_S"
  val FLOW_L_S = "FLOW_L_S"
  val WATER_LEVEL_ABOVE_SEA_LEVEL = "WATER_LEVEL_ABOVE_SEA_LEVEL"
  val WATER_LEVEL_IN_M = "WATER_LEVEL_IN_M"
  val TEMPERATURE = "TEMPERATURE"
  val UNKNOWN = "UNKNOWN"

  def fromXMLValue(input: String) = input match {
    case "10" => FLOW_M_3_S
    case "22" => FLOW_L_S
    case "02" => WATER_LEVEL_ABOVE_SEA_LEVEL
    case "01" => WATER_LEVEL_IN_M
    case "03" => TEMPERATURE
    case x    => UNKNOWN + x
  }

}

case class Measurement(
  date: Date,
  current: Option[Double],
  minus24: Option[Double],
  delta24: Option[Double],
  mean24: Option[Double],
  max24: Option[Double],
  min24: Option[Double],
  measurementType: String,
  variant: String,
  dataOwner: String)

object Measurement {
  implicit val measurementFormat = Json.format[Measurement]

  def fromXML(node: Node) = Measurement(
    toDateTime((node \ "Datum").text, (node \ "Zeit").text),
    extractValue(node, "c"),
    extractValue(node, "c", "-24h"),
    extractValue(node, "delta24"),
    extractValue(node, "m24"),
    extractValue(node, "max24"),
    extractValue(node, "min24"),
    MeasurementType.fromXMLValue((node \ "@Typ").text),
    (node \ "@Var").text,
    (node \ "@DH").text)

  def extractValue(node: Node, typAttributeValue: String, dtAttributeValue: String = "0h"): Option[Double] = {
    (node \ "Wert").filter(w => (w \ "@Typ").text == typAttributeValue && (w \ "@dt").text == dtAttributeValue).text match {
      case ""        => None
      case noneEmpty => Some(NumberFormat.getInstance(new Locale("de", "CH")).parse(noneEmpty).doubleValue())
    }
  }

  def toDateTime(date: String, time: String): Date = {
    val dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm")

    try {
      dateFormat.parse("%s %s".format(date, time))
    } catch {
      case e: ParseException =>
        Logger.warn(s"Could not parse date/time. Error message: ${e.getMessage}")
        new Date()
    }
  }
}

case class MeasuringStation(stationId: Int, name: String, measurements: Seq[Measurement])

object MeasuringStation {
  implicit val measuringStationFormat = Json.format[MeasuringStation]

  def fromXML(node: Node): Map[Int, MeasuringStation] = {
    val measurements = for {
      mesPar <- (node \ "MesPar")
      name = (mesPar \ "Name").text
    } yield ((mesPar \ "@StrNr").text.toInt, name, Measurement.fromXML(mesPar))

    measurements.groupBy(_._1).map {
      case (id, grp) => (id, grp.head._2, grp.map(_._3)) // keep one name only
    } map {
      case (id, name, ms) => (id -> MeasuringStation(id, name, ms))
    } toMap
  }
}
