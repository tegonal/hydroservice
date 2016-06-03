name := """hydroservice"""

version := "1.1-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

routesGenerator := InjectedRoutesGenerator

libraryDependencies ++= Seq(
  cache,
  specs2 % Test,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.11"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

scalacOptions in ThisBuild ++= Seq("-feature", "-language:postfixOps")
