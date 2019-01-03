name := "hello"
organization := "com.rcruitme"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.6"

crossScalaVersions := Seq("2.12.6", "2.11.12")

val silhouetteVersion = "5.0.5"

libraryDependencies ++= Seq(
  guice,
  jdbc,
  evolutions,
  "com.iheart" %% "ficus" % "1.4.3",
  "com.mohiva" %% "play-silhouette" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-crypto-jca" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-password-bcrypt" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-persistence" % silhouetteVersion,
  "com.typesafe.play" %% "play-mailer" % "6.0.1",
  "com.typesafe.play" %% "play-mailer-guice" % "6.0.1",
  "net.codingwell" %% "scala-guice" % "4.1.0",
  "org.postgresql" % "postgresql" % "42.2.5",
  "org.playframework.anorm" %% "anorm" % "2.6.2",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
)
