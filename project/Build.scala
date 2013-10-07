import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "hydroservice"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    "org.reactivemongo" %% "play2-reactivemongo" % "0.10.0-SNAPSHOT",
    "org.reactivemongo" %% "reactivemongo"       % "0.10.0-SNAPSHOT")

  val main = play.Project(appName, appVersion, appDependencies).settings( // Add your own project settings here
    resolvers += "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/")

}
