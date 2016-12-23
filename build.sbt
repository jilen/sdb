name := "sdb"

version := "1.0.0"

scalaVersion := "2.11.8"

javaOptions += "-Dfile.encoding=UTF-8"

fork := true

libraryDependencies ++= Seq(
  "im.drip"                    %% "common-dal"           % "1.3.1-SNAPSHOT",
  "com.typesafe.slick"         %% "slick"                % "3.1.1",
  "ch.qos.logback"             %  "logback-classic"      % "1.1.8",
  "com.storm-enroute"          %% "scalameter"           % "0.7",
  "com.typesafe.slick"         %% "slick-hikaricp"       % "3.1.1",
  "mysql"                      %  "mysql-connector-java" % "5.1.30"
)

packAutoSettings
