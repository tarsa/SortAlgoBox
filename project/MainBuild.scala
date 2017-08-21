/*
 * Copyright (C) 2015 - 2017 Piotr Tarsa ( http://github.com/tarsa )
 *
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will the author be held liable for any damages
 * arising from the use of this software.
 *
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 *
 * 1. The origin of this software must not be misrepresented; you must not
 * claim that you wrote the original software. If you use this software
 * in a product, an acknowledgment in the product documentation would be
 * appreciated but is not required.
 * 2. Altered source versions must be plainly marked as such, and must not be
 * misrepresented as being the original software.
 * 3. This notice may not be removed or altered from any source distribution.
 */

import sbt._
import sbt.Keys._

object MainBuild extends Build {

  val theScalaVersion = "2.11.11"

  lazy val commonSettings = Seq(
    scalaVersion := theScalaVersion,
    conflictManager := ConflictManager.strict,
    scalacOptions += "-feature",
    dependencyOverrides ++= Set(
      "org.scala-lang" % "scala-library" % theScalaVersion,
      "org.scala-lang" % "scala-reflect" % theScalaVersion,
      "org.scala-lang.modules" %% "scala-xml" % "1.0.5",
      "org.scalatest" %% "scalatest" % "3.0.1" % Test
    )
  )

  val fullDep = "compile->compile;test->test"

  lazy val deps = Project(id = "deps", base = file("./deps"))
    .settings(commonSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        "com.jsuereth" %% "scala-arm" % "1.4",
        "commons-io" % "commons-io" % "2.5",
        "org.apache.commons" % "commons-math3" % "3.6.1",
        "org.jocl" % "jocl" % "2.0.0",
        "org.scalafx" %% "scalafx" % "8.0.102-R11",
        "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % Test,
        "org.scalatest" %% "scalatest" % "3.0.1" % Test
      )
    )

  lazy val depsCp = deps % fullDep

  lazy val rootDeps =
    Seq(common, core, crossVerify, fxGui, natives, openCl, random, sorts)

  lazy val root = Project(id = "SortingAlgorithmsToolbox", base = file("."))
    .settings(version := "0.1", name := "SortingAlgorithmsToolbox")
    .settings(commonSettings: _*)
    .aggregate(rootDeps.map(Project.projectToRef): _*)
    .dependsOn(rootDeps.map(p => ClasspathDependency(p, Some(fullDep))): _*)
    .settings(rootDeps.map { dep =>
      discoveredMainClasses in Compile ++=
        (discoveredMainClasses in dep in Compile).value
    }: _*)

  lazy val common =
    Project(id = "common", base = file("./common"))
      .settings(commonSettings: _*)
      .dependsOn(depsCp)

  lazy val commonCp = common % fullDep

  lazy val core =
    Project(id = "core", base = file("./core"))
      .settings(commonSettings: _*)
      .dependsOn(commonCp, nativesCp, openClCp, randomCp)

  lazy val coreCp = core % fullDep

  lazy val crossVerify =
    Project(id = "crossverify", base = file("./crossverify"))
      .settings(commonSettings: _*)
      .dependsOn(commonCp, nativesCp, openClCp, randomCp, sortsCp)

  lazy val crossVerifyCp = crossVerify % fullDep

  lazy val fxGui =
    Project(id = "fxgui", base = file("./fxgui"))
      .settings(commonSettings: _*)
      .dependsOn(commonCp, coreCp)

  lazy val fxGuiCp = fxGui % fullDep

  lazy val natives =
    Project(id = "natives", base = file("./natives"))
      .settings(commonSettings: _*)
      .dependsOn(commonCp)

  lazy val nativesCp = natives % fullDep

  lazy val openCl =
    Project(id = "opencl", base = file("./opencl"))
      .settings(commonSettings: _*)
      .dependsOn(commonCp)

  lazy val openClCp = openCl % fullDep

  lazy val random =
    Project(id = "random", base = file("./random"))
      .settings(commonSettings: _*)
      .dependsOn(commonCp, nativesCp, openClCp)

  lazy val randomCp = random % fullDep

  lazy val sorts =
    Project(id = "sorts", base = file("./sorts"))
      .settings(commonSettings: _*)
      .dependsOn(commonCp, coreCp, nativesCp)

  lazy val sortsCp = sorts % fullDep
}
