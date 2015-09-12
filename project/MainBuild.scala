import sbt._
import Keys._

object MainBuild extends Build {

  val theScalaVersion = "2.11.7"

  lazy val commonSettings = Seq(
    scalaVersion := theScalaVersion,
    conflictManager := ConflictManager.strict,
    dependencyOverrides ++= Set(
      "org.scala-lang" % "scala-library" % theScalaVersion,
      "org.scala-lang" % "scala-reflect" % theScalaVersion,
      "org.scala-lang.modules" %% "scala-xml" % "1.0.5")
  )

  val fullDep = "compile->compile;test->test"

  lazy val deps = Project(id = "deps", base = file("./deps"))
    .settings(commonSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        "org.apache.commons" % "commons-math3" % "3.5",
        "org.jocl" % "jocl" % "0.1.9",
        "org.scalatest" %% "scalatest" % "2.2.4" % "test",
        "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % "test",
        "org.scalafx" %% "scalafx" % "8.0.40-R8")
    )

  lazy val depsCp = deps % fullDep

  lazy val rootDeps = Seq(core, fxgui, natives, opencl, random, sorts)

  lazy val root = Project(id = "SortingAlgorithmsToolbox", base = file("."))
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
    .dependsOn(depsCp, nativesCp, opencl, random)

  lazy val fxgui = Project(id = "fxgui", base = file("./fxgui"))
    .settings(commonSettings: _*)
    .dependsOn(depsCp, core)

  lazy val natives = Project(id = "natives", base = file("./natives"))
    .settings(commonSettings: _*)
    .dependsOn(depsCp)

  lazy val nativesCp = natives % fullDep

  lazy val opencl = Project(id = "opencl", base = file("./opencl"))
    .settings(commonSettings: _*)
    .dependsOn(depsCp)

  lazy val random = Project(id = "random", base = file("./random"))
    .settings(commonSettings: _*)
    .dependsOn(depsCp, nativesCp, opencl)

  lazy val sorts = Project(id = "sorts", base = file("./sorts"))
    .settings(commonSettings: _*)
    .dependsOn(depsCp, core, nativesCp)
}
