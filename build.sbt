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
  "org.playframework.anorm" %% "anorm" % "2.6.2",
  "org.postgresql" % "postgresql" % "42.2.5",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % Test
)
