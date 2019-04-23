name := "Voice-Tests"

version := "0.1"

scalaVersion := "2.12.8"

val akkaVersion      = "2.5.16"
val akkaHttpVersion  = "10.1.5"

libraryDependencies ++= Seq(
  "com.typesafe.akka"      %% "akka-actor"           % akkaVersion,
  "com.typesafe.akka"      %% "akka-slf4j"           % akkaVersion,
  "com.typesafe.akka"      %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka"      %% "akka-http-spray-json" % akkaHttpVersion,
  "io.spray"               %% "spray-json"           % "1.3.3",
  "org.scala-lang.modules" %% "scala-xml"            % "1.1.1",
  "com.typesafe"           %  "config"               % "1.3.1",
  "com.typesafe.akka"      %% "akka-stream"          % akkaVersion,
  )