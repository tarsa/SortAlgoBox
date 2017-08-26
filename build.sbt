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

lazy val directoriesLayoutSettings =
  Seq(
    sourceDirectory in Compile := baseDirectory.value / "main_src",
    sourceDirectory in Test := baseDirectory.value / "test_src",
    resourceDirectory in Compile := baseDirectory.value / "main_rsrc",
    resourceDirectory in Test := baseDirectory.value / "test_rsrc",
    scalaSource in Compile := baseDirectory.value / "main_scala",
    scalaSource in Test := baseDirectory.value / "test_scala",
    javaSource in Compile := baseDirectory.value / "main_java",
    javaSource in Test := baseDirectory.value / "test_java"
  )

lazy val commonSettings =
  Seq(
    scalaVersion := Versions.theScala,
    conflictManager := ConflictManager.strict,
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked"),
    dependencyOverrides ++= Seq(
      "org.scala-lang" % "scala-library" % Versions.theScala,
      "org.scala-lang" % "scala-reflect" % Versions.theScala,
      "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
      "org.scalatest" %% "scalatest" % "3.0.1" % Test
    )
  ) ++ directoriesLayoutSettings ++ Special.commonSettings

val fullDep = "compile->compile;test->test"

//noinspection SbtReplaceProjectWithProjectIn
lazy val deps =
  Project("deps", file("./deps"))
    .settings(commonSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        // production libraries
        "com.typesafe.akka" %% "akka-actor" % Versions.akka,
        "com.jsuereth" %% "scala-arm" % "2.0",
        "commons-io" % "commons-io" % "2.5",
        "org.apache.commons" % "commons-math3" % "3.6.1",
        "org.scalafx" %% "scalafx" % "8.0.102-R11",
        // test libraries
        "com.typesafe.akka" %% "akka-testkit" % Versions.akka % Test,
        "org.scalatest" %% "scalatest" % "3.0.1" % Test
      )
    )
    .settings(Special.depsSettings: _*)

lazy val depsCp = deps % fullDep

lazy val rootDeps = boot +: bootDeps

lazy val root = Project("SortingAlgorithmsToolbox", file("."))
  .settings(version := "0.1")
  .settings(commonSettings: _*)
  .aggregate(rootDeps.map(Project.projectToRef): _*)
  .dependsOn(rootDeps.map(p => ClasspathDependency(p, Some(fullDep))): _*)
  .settings(rootDeps.map { dep =>
    discoveredMainClasses in Compile ++=
      (discoveredMainClasses in dep in Compile).value
  }: _*)

lazy val bootDeps =
  Seq(common, core, crossVerify, fxGui, natives, openCl, random, sorts)

//noinspection SbtReplaceProjectWithProjectIn
lazy val boot =
  Project("boot", file("./boot"))
    .settings(commonSettings: _*)
    .dependsOn(bootDeps.map(p => ClasspathDependency(p, Some(fullDep))): _*)

//noinspection SbtReplaceProjectWithProjectIn
lazy val common =
  Project("common", file("./common"))
    .settings(commonSettings: _*)
    .dependsOn(depsCp)

lazy val commonCp = common % fullDep

//noinspection SbtReplaceProjectWithProjectIn
lazy val core =
  Project("core", file("./core"))
    .settings(commonSettings: _*)
    .dependsOn(commonCp, nativesCp, openClCp, randomCp)

lazy val coreCp = core % fullDep

lazy val crossVerify =
  Project("crossverify", file("./crossverify"))
    .settings(commonSettings: _*)
    .dependsOn(commonCp, nativesCp, openClCp, randomCp, sortsCp)

lazy val crossVerifyCp = crossVerify % fullDep

lazy val fxGui =
  Project("fxgui", file("./fxgui"))
    .settings(commonSettings: _*)
    .dependsOn(commonCp, coreCp)

lazy val fxGuiCp = fxGui % fullDep

//noinspection SbtReplaceProjectWithProjectIn
lazy val natives =
  Project("natives", file("./natives"))
    .settings(commonSettings: _*)
    .dependsOn(commonCp)

lazy val nativesCp = natives % fullDep

lazy val openCl =
  Project("opencl", file("./opencl"))
    .settings(commonSettings: _*)
    .dependsOn(commonCp)

lazy val openClCp = openCl % fullDep

//noinspection SbtReplaceProjectWithProjectIn
lazy val random =
  Project("random", file("./random"))
    .settings(commonSettings: _*)
    .dependsOn(commonCp, nativesCp, openClCp)

lazy val randomCp = random % fullDep

//noinspection SbtReplaceProjectWithProjectIn
lazy val sorts =
  Project("sorts", file("./sorts"))
    .settings(commonSettings: _*)
    .dependsOn(commonCp, coreCp, nativesCp)

lazy val sortsCp = sorts % fullDep
