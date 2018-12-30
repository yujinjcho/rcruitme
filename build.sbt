name := "hello"
organization := "com.rcruitme"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.6"

crossScalaVersions := Seq("2.12.6", "2.11.12")

libraryDependencies ++= Seq(
  guice,
  jdbc,
  evolutions,
  "org.postgresql" % "postgresql" % "42.2.5",
  "org.playframework.anorm" %% "anorm" % "2.6.2",
  "com.mohiva" %% "play-silhouette" % "5.0.5",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "5.0.5",
  "com.mohiva" %% "play-silhouette-persistence" % "5.0.5",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "5.0.5",
  "net.codingwell" %% "scala-guice" % "4.1.0",
  "com.iheart" %% "ficus" % "1.4.3",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
)
