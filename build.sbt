ThisBuild / scalaVersion := "2.13.10"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "petomat"
ThisBuild / organizationName := "petomat"

val zioVersion = "2.0.5"

lazy val root = (project in file("."))
  .settings(
    name := "ZIOModbus",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-streams" % zioVersion,
      "dev.zio" %% "zio-test" % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
//      "dev.zio" %% "zio-test-magnolia" % zioVersion % Test,
      "dev.zio" %% "zio-process" % "0.7.1", // % Test,
      "com.fazecast" % "jSerialComm" % "2.9.3"
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    reStart / mainClass := Some("Main4")
  )
