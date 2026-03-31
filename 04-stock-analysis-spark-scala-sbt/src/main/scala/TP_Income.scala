import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

// =============================================================================
//  PARTIE 2 – Analyse du fichier Income avec Spark SQL
//  (équivalent du travail MapReduce de l'évaluation 01 BigData2)
// =============================================================================

object TP_Income {

  // --------------------------------------------------------------------------
  // Modèle de données
  // --------------------------------------------------------------------------
  case class Income(
    id:             Double,
    workclass:      String,
    education:      String,
    maritalstatus:  String,
    occupation:     String,
    relationship:   String,
    race:           String,
    gender:         String,
    nativecountry:  String,
    income:         String,
    age:            Double,
    fnlwgt:         Double,
    educationalnum: Double,
    capitalgain:    Double,
    capitalloss:    Double,
    hoursperweek:   Double
  )

  // --------------------------------------------------------------------------
  // Parseurs
  // --------------------------------------------------------------------------
  def parseIncome(str: String): Income = {
    val col = str.split(",")
    Income(
      col(0).trim.toDouble,
      col(1).trim,
      col(2).trim,
      col(3).trim,
      col(4).trim,
      col(5).trim,
      col(6).trim,
      col(7).trim,
      col(8).trim,
      col(9).trim,
      col(10).trim.toDouble,
      col(11).trim.toDouble,
      col(12).trim.toDouble,
      col(13).trim.toDouble,
      col(14).trim.toDouble,
      col(15).trim.toDouble
    )
  }

  def parseIncomeRDD(rdd: RDD[String]): RDD[Income] = {
    val header = rdd.first()
    rdd.filter(line => line != header).map(parseIncome).cache()
  }

  // --------------------------------------------------------------------------
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("TP Spark SQL – Income")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    import spark.implicits._

    val csvPath = if (args.nonEmpty) args(0) else "data/Income.csv"

    // -----------------------------------------------------------------------
    // Méthode 1 – Lecture directe avec SQLContext / inferSchema
    // -----------------------------------------------------------------------
    println("\n=== Méthode 1 : lecture CSV avec inferSchema ===")
    val df = spark.read
      .format("csv")
      .option("header", "true")
      .option("inferSchema", "true")
      .load(csvPath)

    df.printSchema()
    df.show()

    df.createOrReplaceTempView("income_view")

    println("\n--- Méthode 1 : Moyenne d'âge par catégorie de revenu ---")
    spark.sql(
      """
        |SELECT income,
        |       ROUND(AVG(age), 2) AS moyenne_age
        |FROM income_view
        |GROUP BY income
        |ORDER BY income
      """.stripMargin
    ).show()

    // -----------------------------------------------------------------------
    // Méthode 2 – Lecture via case class + RDD
    // -----------------------------------------------------------------------
    println("\n=== Méthode 2 : lecture via case class RDD ===")
    val incomeDF = parseIncomeRDD(spark.sparkContext.textFile(csvPath))
      .toDF()
      .cache()

    incomeDF.printSchema()
    incomeDF.show()

    incomeDF.createOrReplaceTempView("income_rdd")

    println("\n--- Méthode 2 : Moyenne d'âge par catégorie de revenu ---")
    spark.sql(
      """
        |SELECT income,
        |       ROUND(AVG(age), 2) AS moyenne_age
        |FROM income_rdd
        |GROUP BY income
        |ORDER BY income
      """.stripMargin
    ).show()

    spark.stop()
  }
}
