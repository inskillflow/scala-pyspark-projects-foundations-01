name := "TP-SparkSQL"

version := "1.0"

scalaVersion := "2.12.18"

val sparkVersion = "3.5.1"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core"  % sparkVersion,
  "org.apache.spark" %% "spark-sql"   % sparkVersion,
  "org.apache.spark" %% "spark-mllib" % sparkVersion
)

// Spark 3.5.1 supporte Java 21 nativement - aucun --add-opens nécessaire
