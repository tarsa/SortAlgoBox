import sbt._
import Keys._

object MyBuild extends Build {

  lazy val commonSettings = Seq(
    scalaVersion := "2.11.6",
    unmanagedJars in Compile += Attributed.blank(
      file("/usr/lib/jvm/java-8-oracle/jre/lib/ext/jfxrt.jar"))
  )

  lazy val deps = Project(id = "deps", base = file("./deps"))
    .settings(commonSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        "org.jocl" % "jocl" % "0.1.9",
        "org.scalatest" %% "scalatest" % "2.2.1" % "test",
        "org.scalamock" %% "scalamock-scalatest-support" % "3.2" % "test",
        "org.scalafx" %% "scalafx" % "8.0.40-R8"
      )
    )

  lazy val rootDeps = Seq(core, fxgui)

  lazy val root = Project(id = "parent", base = file("."))
    .settings(
      version := "0.1",
      name := "SortingAlgorithmsToolbox")
    .settings(commonSettings: _*)
    .aggregate(rootDeps.map(Project.projectToRef): _*)
    .dependsOn(rootDeps.map(p => classpathDependency(p)): _*)
    .settings(rootDeps.map(dep => discoveredMainClasses in Compile <++=
    discoveredMainClasses in dep in Compile): _*)

  lazy val core = Project(id = "core", base = file("./core"))
    .settings(commonSettings: _*)
    .dependsOn(deps % "compile->compile;test->test")

  lazy val fxgui = Project(id = "fxgui", base = file("./fxgui"))
    .settings(commonSettings: _*)
    .dependsOn(deps % "compile->compile;test->test", core)
}
