package controllers

import play.api._
import play.api.Play.current
import play.api.mvc._
import play.api.libs.ws.WS
import scala.xml._
import scala.xml.parsing._
import play.api.libs.json._
import play.api.cache.Cache
import scala.concurrent.duration.DurationInt
import play.api.libs.concurrent.Akka
import models._
import reactivemongo.api._
import play.modules.reactivemongo._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats._

object Application extends Controller with MongoController {

  def stationsCollection: JSONCollection = db.collection[JSONCollection]("stations")

  def historyCollection: JSONCollection = db.collection[JSONCollection]("stations_history")

  /**
   * reloading the data into db
   */
  val dataReloader = Akka.system.scheduler.schedule(0 milliseconds, 10 minutes) {
    try {
      persistStationData()
    } catch {
      // do nothing, just wait for the next reload
      case e: Exception => Logger.debug("Got exception while fetching data", e)
    }
  }

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  /**
   * @return a Json array of `MeasuringStation`
   */
  def stations = Action {
    Async {
      stationsCollection.
        find(Json.obj()).
        cursor[MeasuringStation].toList.map { stations =>
          Ok(Json.toJson(stations))
        }
    }
  }

  /**
   * @param id the station id
   * @return a Json representation of a single `MeasuringStation` or `NotFound` if there is no station with the given id
   */
  def station(id: Int) = Action {
    Async {
      stationsCollection.
        find(Json.obj("measuringStationId" -> id)).
        cursor[MeasuringStation].headOption.map(_.map { station =>
          Ok(Json.toJson(station))
        }.getOrElse(NotFound))
    }
  }

  /**
   * @param filter is used to filter the resulting list by station name
   * @return a Json array of (id: Int, name: String) tuples
   */
  def stationList(filter: String) = Action {
    Async {
      val regex = "(?i).*?" + filter + ".*"

      stationsCollection.
        find(Json.obj("name" -> Json.obj("$regex" -> regex))).
        sort(Json.obj("name" -> 1)).
        cursor[MeasuringStation].toList.map { stations =>
          Ok(Json.toJson(stations.map { station =>
            Json.obj(
              "id" -> station.measuringStationId,
              "name" -> station.name)
          }))
        }
    }
  }

  def history(id: Int, from: Long, to: Long) = Action {
    Async {
      historyCollection.
        find(Json.obj("measuringStationId" -> id,
          "measurements" -> Json.obj(
            "$elemMatch" -> Json.obj(
              "date" -> Json.obj(
                "$gte" -> from,
                "$lt" -> to))))).
        cursor[MeasuringStation].toList.map { stations =>
          Ok(Json.prettyPrint(Json.toJson(stations)))
        }
    }
  }

  def persistStationData() = {
    Logger.debug("persisting station data")

    val stationMap = transformXmlToMeasuringStationMap(retrieveData)

    stationMap.map {
      case (id, station) =>
        getByStationId(id).map { result =>
          result.map { existing =>
            if (station != existing) {
              update(station)
            }
          }.getOrElse {
            insert(station)
          }
        }
    }
  }

  def insert(station: MeasuringStation) = {
    stationsCollection.insert(station)
    updateHistory(station)
  }

  def update(station: MeasuringStation) = {
    stationsCollection.update(
      Json.obj("measuringStationId" -> station.measuringStationId),
      Json.obj("$set" -> station)).map { _ => () }

    updateHistory(station)
  }

  def updateHistory(station: MeasuringStation) = {
    historyCollection.insert(station)
  }

  def getByStationId(id: Int) = {
    stationsCollection.find(Json.obj("measuringStationId" -> id)).cursor[JsObject].headOption.map(_.map(js => (js.as[MeasuringStation], id)))
  }

  def retrieveData =
    scala.xml.XML.load(Play.current.configuration.getString("hydro.source").get)

  def transformXmlToMeasuringStationMap(input: scala.xml.Node): Map[Int, MeasuringStation] =
    MeasuringStation.fromXML(input)

}