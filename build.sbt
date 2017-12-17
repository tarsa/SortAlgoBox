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
import org.scalajs.sbtplugin.cross.CrossProject

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
      "com.typesafe.akka" %% "akka-actor" % Versions.akka,
      "com.typesafe.akka" %% "akka-stream" % Versions.akka,
      "com.typesafe.akka" %% "akka-stream-testkit" % Versions.akka % Test,
      "com.typesafe.akka" %% "akka-testkit" % Versions.akka % Test,
      "org.scala-lang" % "scala-library" % Versions.theScala,
      "org.scala-lang" % "scala-reflect" % Versions.theScala,
      "org.scala-lang.modules" %% "scala-xml" % "1.0.6",
      "org.scalatest" %% "scalatest" % Versions.scalaTest % Test
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
        "com.typesafe.akka" %% "akka-http" % Versions.akkaHttp,
        "com.jsuereth" %% "scala-arm" % "2.0",
        "commons-io" % "commons-io" % "2.6",
        "org.apache.commons" % "commons-math3" % "3.6.1",
        "org.scalafx" %% "scalafx" % Versions.scalaFx,
        // test libraries
        "com.typesafe.akka" %% "akka-http-testkit" % Versions.akkaHttp % Test,
        "com.typesafe.akka" %% "akka-testkit" % Versions.akka % Test,
        "org.scalatest" %% "scalatest" % Versions.scalaTest % Test
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
  Seq(common,
      core,
      crossVerify,
      fxGui,
      natives,
      openCl,
      random,
      sharedJvm,
      sorts)

//noinspection SbtReplaceProjectWithProjectIn
lazy val boot =
  Project("boot", file("./boot"))
    .settings(commonSettings: _*)
    .settings(mainClass in reStart := Some(
      "pl.tarsa.sortalgobox.main.app.WebServerBenchmarkSuiteApp"))
    .settings(
      scalaJSProjects := Seq(frontend),
      pipelineStages in Assets := Seq(scalaJSPipeline),
      // triggers scalaJSPipeline when using compile or continuous compilation
      compile in Compile := ((compile in Compile) dependsOn scalaJSPipeline).value,
      WebKeys.packagePrefix in Assets := "public/",
      managedClasspath in Runtime += (packageBin in Assets).value
    )
    .enablePlugins(SbtWeb)
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

lazy val shared =
  CrossProject("shared", file("./shared"), CrossType.Pure)
    .settings(commonSettings: _*)
    .settings(
      libraryDependencies ++= Seq(
        // production libraries
        "com.lihaoyi" %%% "scalatags" % "0.6.7"
      ))

lazy val sharedJvm = shared.jvm

lazy val sharedJvmCp = sharedJvm % fullDep

lazy val sharedJs = shared.js

lazy val sharedJsCp = sharedJs % fullDep

//noinspection SbtReplaceProjectWithProjectIn
lazy val frontend =
  Project("frontend", file("./frontend"))
    .settings(commonSettings: _*)
    .settings(scalaJSUseMainModuleInitializer := true,
              scalaJSUseMainModuleInitializer in Test := false)
    .settings(
      dependencyOverrides ++= Seq(
        "com.github.japgolly.scalajs-react" %%% "core" % "1.1.0",
        "org.scala-js" %% "scalajs-library" % "0.6.19",
        "org.scala-js" %%% "scalajs-dom" % "0.9.3"))
    .settings(
      libraryDependencies ++= Seq(
        "com.github.japgolly.scalajs-react" %%% "core" % "1.1.0",
        "io.suzaku" %%% "diode" % "1.1.2",
        "io.suzaku" %%% "diode-react" % "1.1.2",
        "org.scala-js" %%% "scalajs-dom" % "0.9.3"
      ),
      jsDependencies ++= Seq(
        "org.webjars.bower" % "react" % "15.6.1"
          / "react-with-addons.js"
          minified "react-with-addons.min.js"
          commonJSName "React",
        "org.webjars.bower" % "react" % "15.6.1"
          / "react-dom.js"
          minified "react-dom.min.js"
          dependsOn "react-with-addons.js"
          commonJSName "ReactDOM"
      )
    )
    .enablePlugins(ScalaJSPlugin, ScalaJSWeb)
    .dependsOn(sharedJsCp)
