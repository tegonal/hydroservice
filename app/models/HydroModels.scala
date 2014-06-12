package models

import xml._
import java.text.SimpleDateFormat
import java.util.Date
import scala.collection.mutable.MapBuilder
import play.api.libs.json._
import play.api.libs.functional.syntax._
import java.text.NumberFormat
import java.util.Locale

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
    case x => UNKNOWN + x
  }

}

case class Measurement(
  date: Date,
  current: Double,
  minus24: Double,
  delta24: Double,
  mean24: Double,
  max24: Double,
  min24: Double,
  measurementType: String,
  variant: String,
  dataOwner: String)

object Measurement {
  implicit val measurementFormat = Json.format[Measurement]

  def fromXML(node: Node) = Measurement(
    toDateTime((node \ "Datum").text, (node \ "Zeit").text),
    extractValue(node, ""),
    extractValue(node, "", "-24h"),
    extractValue(node, "delta24"),
    extractValue(node, "m24"),
    extractValue(node, "max24"),
    extractValue(node, "min24"),
    MeasurementType.fromXMLValue((node \ "@Typ").text),
    (node \ "@Var").text,
    (node \ "@DH").text)

  def extractValue(node: Node, typAttributeValue: String, dtAttributeValue: String = ""): Double = {
    (node \ "Wert").filter(w => (w \ "@Typ").text == typAttributeValue && (w \ "@dt").text == dtAttributeValue).text match {
      case "" => Double.NaN
      case noneEmpty => NumberFormat.getInstance(new Locale("de", "CH")).parse(noneEmpty).doubleValue()
    }
  }

  def toDateTime(date: String, time: String): Date = {
    val dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm")

    dateFormat.parse("%s %s".format(date, time))
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
