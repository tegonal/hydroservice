package controllers

import play.api._
import play.api.mvc._
import scala.xml._
import scala.xml.parsing._
import play.api.libs.json._
import scala.concurrent.duration.DurationInt
import akka.actor._
import models._
import reactivemongo.api._
import play.modules.reactivemongo._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api._
import reactivemongo.bson._
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo._
import reactivemongo.api.ReadPreference
import reactivemongo.play.json._
import reactivemongo.play.json.collection._
import play.modules.reactivemongo.json.BSONFormats._
import javax.inject._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class Application @Inject() (val reactiveMongoApi: ReactiveMongoApi, system: ActorSystem, configuration: Configuration) extends Controller with MongoController with ReactiveMongoComponents {

  val stationsCollection: Future[JSONCollection] = database.map(_.collection[JSONCollection]("stations"))

  val historyCollection: Future[JSONCollection] = database.map(_.collection[JSONCollection]("stations_history"))

  val STATION_ID = "stationId"

  /**
   * reloading the data into db
   */
  val dataReloader = system.scheduler.schedule(0 milliseconds, 10 minutes) {
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
  def stations = Action.async {
    val cursor = stationsCollection.flatMap { stations =>
      stations.find(Json.obj()).
        cursor[MeasuringStation](ReadPreference.primary).collect[List]()
    }
    cursor map { stations =>
      Ok(Json.toJson(stations))
    }
  }

  /**
   * @param id the station id
   * @return a Json representation of a single `MeasuringStation` or `NotFound` if there is no station with the given id
   */
  def station(id: String) = Action.async {
    stationsCollection.flatMap { stations =>
      stations.find(Json.obj(STATION_ID -> id)).
        cursor[MeasuringStation].headOption.map(_.map { station =>
          Ok(Json.toJson(station))
        }.getOrElse(NotFound))
    }
  }

  /**
   * @param filter is used to filter the resulting list by station name
   * @return a Json array of (id: Int, name: String) tuples
   */
  def stationList(filter: String) = Action.async {
    val regex = "(?i).*?" + filter + ".*"

    stationsCollection.flatMap { stations =>
      stations.find(Json.obj("name" -> Json.obj("$regex" -> regex))).
        sort(Json.obj("name" -> 1)).
        cursor[MeasuringStation].collect[List]().map { stations =>
          Ok(Json.toJson(stations.map { station =>
            Json.obj(
              STATION_ID -> station.stationId,
              "name" -> station.name)
          }))
        }
    }
  }

  def history(id: String, from: Long, to: Long) = Action.async {
    historyCollection.flatMap { history =>
      history.find(Json.obj(STATION_ID -> id,
        "measurements" -> Json.obj(
          "$elemMatch" -> Json.obj(
            "date" -> Json.obj(
              "$gte" -> from,
              "$lt" -> to))))).
        cursor[MeasuringStation].collect[List]().map { stations =>
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
    stationsCollection.flatMap { stations =>
      stations.insert(station)
      updateHistory(station)
    }
  }

  def update(station: MeasuringStation) = {
    stationsCollection.flatMap { stations =>
      stations.update(
        Json.obj(STATION_ID -> station.stationId),
        Json.obj("$set" -> station)).map { _ => () }
      updateHistory(station)
    }
  }

  def updateHistory(station: MeasuringStation) = {
    historyCollection.flatMap { history =>
      history.insert(station)
    }
  }

  def getByStationId(id: String) = {
    val cursor = stationsCollection.flatMap { stations =>
      stations.find(Json.obj(STATION_ID -> id))
        .cursor[MeasuringStation](ReadPreference.primary).collect[List]()
    }
    cursor map { stations =>
      stations.headOption
    }
  }

  def retrieveData =
    scala.xml.XML.load(configuration.getString("hydro.source").get)

  def transformXmlToMeasuringStationMap(input: scala.xml.Node): Map[String, MeasuringStation] =
    MeasuringStation.fromXML(input)

}
