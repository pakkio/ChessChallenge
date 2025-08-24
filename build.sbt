name := "ChessChallenge"
version := "1.0"
scalaVersion := "2.12.19"

enablePlugins(NativeImagePlugin)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.17" % "test",
  "com.github.haifengl" % "smile-core" % "2.6.0",
  "com.github.tototoshi" %% "scala-csv" % "1.3.10",
  "org.jcuda" % "jcuda" % "12.0.0",
  "org.jcuda" % "jcuda-natives" % "12.0.0",
  "org.jcuda" % "jcurand" % "12.0.0"
)

// GraalVM Native Image settings
Compile / mainClass := Some("pakkio.chesschallenge.TestBestSolution")
nativeImageOptions ++= Seq(
  "--no-fallback",
  "--report-unsupported-elements-at-runtime",
  "-H:+ReportExceptionStackTraces",
  "--initialize-at-run-time=scala.util.Random",
  "--allow-incomplete-classpath"
)
