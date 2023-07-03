name := "WebFrontEnd"

version := "1.0"

scalaVersion := "2.13.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.2.4",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.4",
  "com.typesafe.akka" %% "akka-stream" % "2.6.14",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.2.4" % Test,
  "org.scalatest" %% "scalatest" % "3.2.9" % Test
)

unmanagedResourceDirectories in Compile <+= baseDirectory(_ / "src/main/resources/public")

enablePlugins(JavaAppPackaging)