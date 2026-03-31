import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

// =============================================================================
//  CORRECTION TP1 — Analyse de l'action AAPL avec Spark SQL
// =============================================================================

object Correction_TP1 {

  // --------------------------------------------------------------------------
  // Étape 1 — Modèle de données
  // --------------------------------------------------------------------------
  case class Stock(
    Date:     String,
    Open:     Double,
    High:     Double,
    Low:      Double,
    Close:    Double,
    Volume:   Double,
    AdjClose: Double
  )

  def parseStock(line: String): Stock = {
    val col = line.split(",")
    Stock(
      col(0).trim,
      col(1).trim.toDouble,
      col(2).trim.toDouble,
      col(3).trim.toDouble,
      col(4).trim.toDouble,
      col(5).trim.toDouble,
      col(6).trim.toDouble
    )
  }

  def parseRDD(rdd: RDD[String]): RDD[Stock] = {
    val header = rdd.first()
    rdd.filter(line => line != header).map(parseStock).cache()
  }

  // --------------------------------------------------------------------------
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("TP1 Correction – Stocks AAPL")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")
    import spark.implicits._

    val csvPath = if (args.nonEmpty) args(0) else "data/AAPL.csv"

    // -----------------------------------------------------------------------
    // Chargement du DataFrame et création de la vue temporaire
    // -----------------------------------------------------------------------
    val stocksDF = parseRDD(spark.sparkContext.textFile(csvPath)).toDF().cache()

    println("=== Schéma du DataFrame ===")
    stocksDF.printSchema()

    println("=== Aperçu des données ===")
    stocksDF.show(20)

    stocksDF.createOrReplaceTempView("stocks")

    // -----------------------------------------------------------------------
    // Requête 1 — Dates, prix d'ouverture et clôture
    // -----------------------------------------------------------------------
    println("\n=== Requête 1 : Dates, ouverture et clôture ===")
    spark.sql(
      """
        |SELECT Date, Open, Close
        |FROM stocks
        |ORDER BY Date ASC
      """.stripMargin
    ).show(20)

    // -----------------------------------------------------------------------
    // Requête 2 — Variation journalière (Close - Open)
    // -----------------------------------------------------------------------
    println("\n=== Requête 2 : Variation journalière (Close - Open) ===")
    spark.sql(
      """
        |SELECT Date,
        |       ROUND(Close - Open, 2) AS variation
        |FROM stocks
        |ORDER BY Date ASC
      """.stripMargin
    ).show(20)

    // -----------------------------------------------------------------------
    // Requête 3 — Volume maximum et minimum
    // -----------------------------------------------------------------------
    println("\n=== Requête 3 : Volume max et min ===")
    spark.sql(
      """
        |SELECT MAX(Volume) AS max_volume,
        |       MIN(Volume) AS min_volume
        |FROM stocks
      """.stripMargin
    ).show()

    // -----------------------------------------------------------------------
    // Requête 4 — Moyenne du prix d'ouverture par année
    // -----------------------------------------------------------------------
    println("\n=== Requête 4 : Moyenne d'ouverture par année ===")
    spark.sql(
      """
        |SELECT SUBSTRING(Date, 1, 4)        AS annee,
        |       ROUND(AVG(Open), 2)           AS moyenne_ouverture
        |FROM stocks
        |GROUP BY SUBSTRING(Date, 1, 4)
        |ORDER BY annee ASC
      """.stripMargin
    ).show()

    // -----------------------------------------------------------------------
    // Requête 5 — Somme des volumes par mois
    // -----------------------------------------------------------------------
    println("\n=== Requête 5 : Somme des volumes par mois ===")
    spark.sql(
      """
        |SELECT SUBSTRING(Date, 6, 2)          AS mois,
        |       CAST(SUM(Volume) AS BIGINT)     AS total_volume
        |FROM stocks
        |GROUP BY SUBSTRING(Date, 6, 2)
        |ORDER BY mois ASC
      """.stripMargin
    ).show()

    // -----------------------------------------------------------------------
    // Requête 6 — Top 5 des journées avec le prix de clôture le plus élevé
    // -----------------------------------------------------------------------
    println("\n=== Requête 6 : Top 5 journées — prix de clôture le plus élevé ===")
    spark.sql(
      """
        |SELECT Date, Close
        |FROM stocks
        |ORDER BY Close DESC
        |LIMIT 5
      """.stripMargin
    ).show()

    // -----------------------------------------------------------------------
    // Requête 7 (BONUS) — Stats annuelles : max, min, moyenne du Close
    // -----------------------------------------------------------------------
    println("\n=== Requête 7 (Bonus) : Stats annuelles du prix de clôture ===")
    spark.sql(
      """
        |SELECT SUBSTRING(Date, 1, 4)    AS annee,
        |       ROUND(MAX(Close), 2)     AS max_close,
        |       ROUND(MIN(Close), 2)     AS min_close,
        |       ROUND(AVG(Close), 2)     AS avg_close
        |FROM stocks
        |GROUP BY SUBSTRING(Date, 1, 4)
        |ORDER BY annee ASC
      """.stripMargin
    ).show()

    spark.stop()
  }
}
