name := "sdb"

version := "1.0.0"

scalaVersion := "2.11.7"

javaOptions += "-Dfile.encoding=UTF-8"

fork := false

libraryDependencies ++= Seq(
  "io.getquill"                %% "quill-async"          % "0.3.0",
  "com.typesafe.slick"         %% "slick"                % "3.1.1",
  "com.storm-enroute"          %% "scalameter"           % "0.7",
  "com.typesafe.slick"         %% "slick-hikaricp"       % "3.1.1",
  "mysql"                      %  "mysql-connector-java" % "5.1.30"
)

packAutoSettings
