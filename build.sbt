name := "SprayRestDemo"

version := "1.0"

scalaVersion := "2.11.2"
libraryDependencies ++= {
  val akkaV = "2.3.6"
  val sprayV = "1.3.2"
  val gsonV = "1.7.1"
  Seq(
    "io.spray"             %%   "spray-can"             % sprayV withSources() withJavadoc(),
    "io.spray"             %%   "spray-routing"         % sprayV withSources() withJavadoc(),
    "io.spray"             %%  "spray-json"             % "1.3.1",
    "io.spray"             %%  "spray-testkit"          % sprayV  % "test",
    "com.typesafe.akka"    %%  "akka-actor"             % akkaV,
    "com.typesafe.akka"    %%  "akka-testkit"           % akkaV   % "test",
    "org.specs2"           %%  "specs2-core"            % "2.3.11" % "test",
    "org.scalaz"           %%  "scalaz-core"            % "7.1.0",
    "org.mongodb.scala"    %% "mongo-scala-driver"       % "1.2.1",
    "com.google.code.gson" % "gson"                     % gsonV

  )
}