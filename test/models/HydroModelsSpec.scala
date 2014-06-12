package models

import org.specs2.mutable._
import models._
import org.joda.time.DateTime
import play.api.libs.json.Json
import models.MeasurementType._

class HydroModelsSpec extends Specification {
  val singleMeasurement = <MesPar Var="00" Typ="02" StrNr="2416" DH="HBCHa">
                            <Name>Aabach - Hitzkirch</Name>
                            <Datum>16.07.2013</Datum>
                            <Zeit>09:00</Zeit>
                            <Wert>462.95</Wert>
                            <Wert dt="-24h">462.95</Wert>
                            <Wert Typ="delta24">-0.006</Wert>
                            <Wert Typ="m24">462.95</Wert>
                            <Wert Typ="max24">462.95</Wert>
                            <Wert Typ="min24">462.94</Wert>
                          </MesPar>

  val multipleMeasurements = <AKT_Data ZeitSt="16.07.2013 10:12" ID="SMS-Liste">
                               <MesPar Var="00" Typ="03" StrNr="2135" DH="HBCHa">
                                 <Name>Aare - Bern</Name>
                                 <Datum>16.07.2013</Datum>
                                 <Zeit>10:00</Zeit>
                                 <Wert>16.17</Wert>
                                 <Wert dt="-24h">16.05</Wert>
                                 <Wert Typ="delta24">0.120</Wert>
                                 <Wert Typ="m24">16.95</Wert>
                                 <Wert Typ="max24">18.43</Wert>
                                 <Wert Typ="min24">15.47</Wert>
                               </MesPar>
                               <MesPar Var="00" Typ="02" StrNr="2135" DH="HBCHa">
                                 <Name>Aare - Bern</Name>
                                 <Datum>16.07.2013</Datum>
                                 <Zeit>10:00</Zeit>
                                 <Wert>502.46</Wert>
                                 <Wert dt="-24h">502.53</Wert>
                                 <Wert Typ="delta24">-0.068</Wert>
                                 <Wert Typ="m24">502.50</Wert>
                                 <Wert Typ="max24">502.53</Wert>
                                 <Wert Typ="min24">502.46</Wert>
                               </MesPar>
                               <MesPar Var="10" Typ="10" StrNr="2135" DH="HBCHa">
                                 <Name>Aare - Bern</Name>
                                 <Datum>16.07.2013</Datum>
                                 <Zeit>10:00</Zeit>
                                 <Wert>156.96</Wert>
                                 <Wert dt="-24h">167.31</Wert>
                                 <Wert Typ="delta24">-10.354</Wert>
                                 <Wert Typ="m24">163.79</Wert>
                                 <Wert Typ="max24">167.78</Wert>
                                 <Wert Typ="min24">156.96</Wert>
                               </MesPar>
                               <MesPar Var="00" Typ="03" StrNr="2019" DH="HBCHa">
                                 <Name>Aare - Brienzwiler</Name>
                                 <Datum>16.07.2013</Datum>
                                 <Zeit>10:00</Zeit>
                                 <Wert>7.32</Wert>
                                 <Wert dt="-24h">7.44</Wert>
                                 <Wert Typ="delta24">-0.120</Wert>
                                 <Wert Typ="m24">8.42</Wert>
                                 <Wert Typ="max24">9.47</Wert>
                                 <Wert Typ="min24">7.32</Wert>
                               </MesPar>
                               <MesPar Var="00" Typ="02" StrNr="2019" DH="HBCHa">
                                 <Name>Aare - Brienzwiler</Name>
                                 <Datum>16.07.2013</Datum>
                                 <Zeit>10:00</Zeit>
                                 <Wert>570.90</Wert>
                                 <Wert dt="-24h">570.86</Wert>
                                 <Wert Typ="delta24">0.042</Wert>
                                 <Wert Typ="m24">570.75</Wert>
                                 <Wert Typ="max24">570.94</Wert>
                                 <Wert Typ="min24">570.39</Wert>
                               </MesPar>
                               <MesPar Var="10" Typ="10" StrNr="2019" DH="HBCHa">
                                 <Name>Aare - Brienzwiler</Name>
                                 <Datum>16.07.2013</Datum>
                                 <Zeit>10:00</Zeit>
                                 <Wert>77.68</Wert>
                                 <Wert dt="-24h">75.06</Wert>
                                 <Wert Typ="delta24">2.621</Wert>
                                 <Wert Typ="m24">68.58</Wert>
                                 <Wert Typ="max24">80.02</Wert>
                                 <Wert Typ="min24">47.78</Wert>
                               </MesPar>
                               <MesPar Var="00" Typ="03" StrNr="2016" DH="HBCHa">
                                 <Name>Aare - Brugg</Name>
                                 <Datum>16.07.2013</Datum>
                                 <Zeit>10:00</Zeit>
                                 <Wert>19.27</Wert>
                                 <Wert dt="-24h">18.89</Wert>
                                 <Wert Typ="delta24">0.380</Wert>
                                 <Wert Typ="m24">19.36</Wert>
                                 <Wert Typ="max24">19.69</Wert>
                                 <Wert Typ="min24">18.86</Wert>
                               </MesPar>
                               <MesPar Var="00" Typ="02" StrNr="2016" DH="HBCHa">
                                 <Name>Aare - Brugg</Name>
                                 <Datum>16.07.2013</Datum>
                                 <Zeit>10:00</Zeit>
                                 <Wert>332.38</Wert>
                                 <Wert dt="-24h">332.45</Wert>
                                 <Wert Typ="delta24">-0.067</Wert>
                                 <Wert Typ="m24">332.45</Wert>
                                 <Wert Typ="max24">332.49</Wert>
                                 <Wert Typ="min24">332.38</Wert>
                               </MesPar>
                               <MesPar Var="10" Typ="10" StrNr="2016" DH="HBCHa">
                                 <Name>Aare - Brugg</Name>
                                 <Datum>16.07.2013</Datum>
                                 <Zeit>10:00</Zeit>
                                 <Wert>278.64</Wert>
                                 <Wert dt="-24h">287.84</Wert>
                                 <Wert Typ="delta24">-9.197</Wert>
                                 <Wert Typ="m24">287.64</Wert>
                                 <Wert Typ="max24">293.82</Wert>
                                 <Wert Typ="min24">277.96</Wert>
                               </MesPar>
                             </AKT_Data>

  val differentNamesButSameId = <AKT_Data ZeitSt="16.07.2013 10:12" ID="SMS-Liste">
                                  <MesPar Var="00" Typ="03" StrNr="2029" DH="HBCHa">
                                    <Name>Aare - Brügg</Name>
                                    <Datum>16.07.2013</Datum>
                                    <Zeit>10:10</Zeit>
                                    <Wert>19.30</Wert>
                                    <Wert dt="-24h">18.84</Wert>
                                    <Wert Typ="delta24">0.460</Wert>
                                    <Wert Typ="m24">19.32</Wert>
                                    <Wert Typ="max24">19.80</Wert>
                                    <Wert Typ="min24">18.88</Wert>
                                  </MesPar>
                                  <MesPar Var="00" Typ="02" StrNr="2029" DH="HBCHa">
                                    <Name>Aare - Brügg</Name>
                                    <Datum>16.07.2013</Datum>
                                    <Zeit>10:10</Zeit>
                                    <Wert>426.80</Wert>
                                    <Wert dt="-24h">426.80</Wert>
                                    <Wert Typ="delta24">0.005</Wert>
                                    <Wert Typ="m24">426.80</Wert>
                                    <Wert Typ="max24">426.80</Wert>
                                    <Wert Typ="min24">426.79</Wert>
                                  </MesPar>
                                  <MesPar Var="30" Typ="10" StrNr="2029" DH="HBCHa">
                                    <Name>Aare - Brügg, Aegerten</Name>
                                    <Datum>16.07.2013</Datum>
                                    <Zeit>10:10</Zeit>
                                    <Wert>238.31</Wert>
                                    <Wert dt="-24h">239.03</Wert>
                                    <Wert Typ="delta24">-0.721</Wert>
                                    <Wert Typ="m24">236.86</Wert>
                                    <Wert Typ="max24">245.65</Wert>
                                    <Wert Typ="min24">230.84</Wert>
                                  </MesPar>
                                </AKT_Data>

  "Measurement" should {
    "be parsed" in {
      val measurement = Measurement.fromXML(singleMeasurement)

      measurement.dataOwner === "HBCHa"
      measurement.variant === "00"
      measurement.measurementType === WATER_LEVEL_ABOVE_SEA_LEVEL
      measurement.date === new DateTime(2013, 7, 16, 9, 0).toDate
      measurement.current === 462.95
      measurement.minus24 === 462.95
      measurement.delta24 === -0.006
      measurement.mean24 === 462.95
      measurement.max24 === 462.95
      measurement.min24 === 462.94
    }

    "be transformed to correct Json representation" in {
      val measurement = Measurement.fromXML(singleMeasurement)

      Json.toJson(measurement) === Json.parse("""
          {
            "date":""" + new DateTime(2013, 7, 16, 9, 0).toDate.getTime() + """,
            "current":462.95,
            "minus24":462.95,
            "delta24":-0.006,
            "mean24":462.95,
            "max24":462.95,
            "min24":462.94,
            "measurementType":"""" + WATER_LEVEL_ABOVE_SEA_LEVEL + """",
            "variant":"00",
            "dataOwner":"HBCHa"
          }
          """)

    }
  }

  "Multiple measurements" should {
    "be parsed" in {
      val measurements = MeasuringStation.fromXML(multipleMeasurements)

      measurements.map(_._1) must containAllOf(List(2135, 2019, 2016))
      measurements.map(_._2.measurements.size) must containAllOf(List(3, 3, 3))
      measurements.map(_._2.name) must containAllOf(List("Aare - Bern", "Aare - Brienzwiler", "Aare - Brugg"))
    }
  }

  "Measurements of the same station id but awkwardly different names" should {
    "be grouped by id" in {
      val measurements = MeasuringStation.fromXML(differentNamesButSameId)

      measurements.size === 1
      measurements.map(_._2.measurements.size) must containAllOf(List(3))
    }
  }
}