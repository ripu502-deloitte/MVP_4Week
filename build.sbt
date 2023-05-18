name := """mvp"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.10"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test



libraryDependencies += "org.reactivemongo" %% "play2-reactivemongo" % "1.0.6-play28"
libraryDependencies += "com.opencsv" % "opencsv" % "5.5"
libraryDependencies ++= Seq(
  "org.mongodb.scala" %% "mongo-scala-driver" % "4.3.1",
  "com.typesafe.play" %% "play-json" % "2.9.2"
)



// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
