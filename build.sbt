name := "dijkstra"

version := "0.1"

scalaVersion := "2.13.8"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % "0.14.1")

libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.2.6"

libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.6.18"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.6.18"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.12" % Test
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.16.0" % Test

coverageExcludedPackages := "com\\.pedrohidalgo\\.trafficflow\\.Main;"
coverageFailOnMinimum := true
coverageMinimumStmtTotal := 90

addCommandAlias("runCoverage", ";clean; coverage; compile; test; coverageReport; coverageOff")

connectInput / run := true

Compile / mainClass := Some("com.pedrohidalgo.trafficflow.Main")
