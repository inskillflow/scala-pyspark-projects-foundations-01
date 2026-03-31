import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.lit

// =============================================================================
//  CORRECTION TP2 — Analyse multi-actions avec Spark SQL
// =============================================================================

object Correction_TP2 {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("TP2 Correction – Multi-Stocks")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    val dataDir = if (args.nonEmpty) args(0) else "data"

    // -----------------------------------------------------------------------
    // Étape 1 — Chargement des 5 fichiers et ajout de la colonne symbol
    // -----------------------------------------------------------------------
    def loadStock(symbol: String) =
      spark.read
        .option("header", "true")
        .option("inferSchema", "true")
        .csv(s"$dataDir/$symbol.csv")
        .withColumn("symbol", lit(symbol))

    val aaplDF = loadStock("AAPL")
    val googDF = loadStock("GOOG")
    val msftDF = loadStock("MSFT")
    val amznDF = loadStock("AMZN")
    val nflxDF = loadStock("NFLX")

    // Fusion des 5 DataFrames
    val allStocksDF = aaplDF
      .union(googDF)
      .union(msftDF)
      .union(amznDF)
      .union(nflxDF)
      .cache()

    println("=== Schéma du DataFrame fusionné ===")
    allStocksDF.printSchema()

    println(s"\n=== Nombre total de lignes : ${allStocksDF.count()} ===")
    allStocksDF.show(5)

    allStocksDF.createOrReplaceTempView("stocks_all")

    // -----------------------------------------------------------------------
    // Requête 1 — Nombre de jours de cotation par action
    // -----------------------------------------------------------------------
    println("\n=== Requête 1 : Nombre de jours de cotation par action ===")
    spark.sql(
      """
        |SELECT symbol,
        |       COUNT(*) AS nb_jours
        |FROM stocks_all
        |GROUP BY symbol
        |ORDER BY nb_jours DESC
      """.stripMargin
    ).show()

    // -----------------------------------------------------------------------
    // Requête 2 — Prix de clôture maximum par action
    // -----------------------------------------------------------------------
    println("\n=== Requête 2 : Prix de clôture maximum par action ===")
    spark.sql(
      """
        |SELECT symbol,
        |       ROUND(MAX(Close), 2) AS max_close
        |FROM stocks_all
        |GROUP BY symbol
        |ORDER BY max_close DESC
      """.stripMargin
    ).show()

    // -----------------------------------------------------------------------
    // Requête 3 — Volume moyen annuel par action (2010 à 2015)
    // -----------------------------------------------------------------------
    println("\n=== Requête 3 : Volume moyen annuel par action (2010-2015) ===")
    spark.sql(
      """
        |SELECT symbol,
        |       SUBSTRING(Date, 1, 4)      AS annee,
        |       ROUND(AVG(Volume), 0)      AS avg_volume
        |FROM stocks_all
        |WHERE SUBSTRING(Date, 1, 4) BETWEEN '2010' AND '2015'
        |GROUP BY symbol, SUBSTRING(Date, 1, 4)
        |ORDER BY symbol, annee
      """.stripMargin
    ).show(50)

    // -----------------------------------------------------------------------
    // Requête 4 — Journée avec le plus grand écart High - Low par action
    // -----------------------------------------------------------------------
    println("\n=== Requête 4 : Volatilité intra-journalière maximale par action ===")
    spark.sql(
      """
        |SELECT symbol, Date,
        |       ROUND(High - Low, 2) AS ecart_max
        |FROM (
        |  SELECT symbol, Date, High, Low,
        |         RANK() OVER (PARTITION BY symbol ORDER BY (High - Low) DESC) AS rang
        |  FROM stocks_all
        |) t
        |WHERE rang = 1
        |ORDER BY ecart_max DESC
      """.stripMargin
    ).show()

    // -----------------------------------------------------------------------
    // Requête 5 — Jours de hausse vs jours de baisse par action
    // -----------------------------------------------------------------------
    println("\n=== Requête 5 : Jours de hausse vs jours de baisse par action ===")
    spark.sql(
      """
        |SELECT symbol,
        |       SUM(CASE WHEN Close > Open THEN 1 ELSE 0 END) AS jours_hausse,
        |       SUM(CASE WHEN Close < Open THEN 1 ELSE 0 END) AS jours_baisse
        |FROM stocks_all
        |GROUP BY symbol
        |ORDER BY symbol
      """.stripMargin
    ).show()

    // -----------------------------------------------------------------------
    // Requête 6 — Jours où AAPL et MSFT ont toutes les deux une hausse
    // -----------------------------------------------------------------------
    println("\n=== Requête 6 : Jours de hausse simultanée AAPL et MSFT ===")

    // Création de deux vues filtrées
    spark.sql(
      """
        |SELECT Date,
        |       ROUND(Close - Open, 2) AS variation
        |FROM stocks_all
        |WHERE symbol = 'AAPL' AND Close > Open
      """.stripMargin
    ).createOrReplaceTempView("aapl_hausse")

    spark.sql(
      """
        |SELECT Date,
        |       ROUND(Close - Open, 2) AS variation
        |FROM stocks_all
        |WHERE symbol = 'MSFT' AND Close > Open
      """.stripMargin
    ).createOrReplaceTempView("msft_hausse")

    spark.sql(
      """
        |SELECT a.Date,
        |       a.variation AS variation_AAPL,
        |       m.variation AS variation_MSFT
        |FROM aapl_hausse a
        |JOIN msft_hausse m ON a.Date = m.Date
        |ORDER BY a.Date DESC
        |LIMIT 20
      """.stripMargin
    ).show()

    spark.stop()
  }
}
