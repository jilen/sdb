name := "sdb"

version := "1.0.0"

scalaVersion := "2.11.7"

javaOptions += "-Dfile.encoding=UTF-8"

libraryDependencies ++= Seq(
  "io.getquill"                %% "quill-async"    % "0.1.0",
  "com.typesafe.slick"         %% "slick"          % "3.1.1",
  "com.storm-enroute"          %% "scalameter"     % "0.7"
)
