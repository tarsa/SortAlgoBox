import sbt._
import Keys._

object MyBuild extends Build {

  lazy val commonSettings = Seq(
    scalaVersion := "2.11.6"
  )

  lazy val deps = Project(id = "deps", base = file("./deps"))
    .settings(commonSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "2.2.1" % "test",
        "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test",
        "org.jocl" % "jocl" % "0.1.9"
      )
    )

  lazy val root = Project(id = "parent", base = file("."))
    .settings(
      version := "0.1",
      name := "SortingAlgorithmsToolbox")
    .settings(commonSettings: _*)
    .aggregate(core)
    .settings(run := (run in core in Compile).evaluated)

  lazy val core = Project(id = "core", base = file("./core"))
    .settings(commonSettings: _*)
    .dependsOn(deps % "compile->compile;test->test")
}
