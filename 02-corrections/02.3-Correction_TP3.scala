import org.apache.spark.sql.SparkSession

// =============================================================================
//  CORRECTION TP3 — Window Functions et indicateurs boursiers (AAPL)
// =============================================================================

object Correction_TP3 {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("TP3 Correction – Window Functions AAPL")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    val csvPath = if (args.nonEmpty) args(0) else "data/AAPL.csv"

    // -----------------------------------------------------------------------
    // Étape 1 — Chargement et statistiques de base
    // -----------------------------------------------------------------------
    val df = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv(csvPath)
      .cache()

    df.createOrReplaceTempView("aapl")

    println("=== Étape 1 : Statistiques générales ===")
    spark.sql(
      """
        |SELECT COUNT(*)        AS nb_jours,
        |       MIN(Date)       AS date_min,
        |       MAX(Date)       AS date_max
        |FROM aapl
      """.stripMargin
    ).show()

    // -----------------------------------------------------------------------
    // Requête 1 — Variation journalière en pourcentage
    // -----------------------------------------------------------------------
    println("\n=== Requête 1 : Variation journalière en % ===")
    spark.sql(
      """
        |SELECT Date,
        |       Close,
        |       LAG(Close, 1) OVER (ORDER BY Date ASC)                    AS close_veille,
        |       ROUND(
        |         (Close - LAG(Close,1) OVER (ORDER BY Date ASC))
        |         / LAG(Close,1) OVER (ORDER BY Date ASC) * 100
        |       , 2)                                                        AS variation_pct
        |FROM aapl
        |ORDER BY Date ASC
      """.stripMargin
    ).show(20)

    // Créer une vue avec la variation pour réutilisation
    spark.sql(
      """
        |SELECT Date, Close,
        |       LAG(Close, 1) OVER (ORDER BY Date ASC)    AS close_veille,
        |       ROUND(
        |         (Close - LAG(Close,1) OVER (ORDER BY Date ASC))
        |         / LAG(Close,1) OVER (ORDER BY Date ASC) * 100
        |       , 2)                                                AS variation_pct
        |FROM aapl
      """.stripMargin
    ).createOrReplaceTempView("aapl_variation")

    // -----------------------------------------------------------------------
    // Requête 2 — Moyenne mobile 7 jours (MA7)
    // -----------------------------------------------------------------------
    println("\n=== Requête 2 : Moyenne mobile 7 jours (MA7) ===")
    spark.sql(
      """
        |SELECT Date,
        |       ROUND(Close, 2) AS Close,
        |       ROUND(
        |         AVG(Close) OVER (
        |           ORDER BY Date ASC
        |           ROWS BETWEEN 6 PRECEDING AND CURRENT ROW
        |         )
        |       , 2) AS ma7
        |FROM aapl
        |ORDER BY Date ASC
      """.stripMargin
    ).show(30)

    // -----------------------------------------------------------------------
    // Requête 3 — Top 10 meilleures journées
    // -----------------------------------------------------------------------
    println("\n=== Requête 3 : Top 10 meilleures journées ===")
    spark.sql(
      """
        |SELECT RANK() OVER (ORDER BY variation_pct DESC) AS rang,
        |       Date,
        |       variation_pct
        |FROM aapl_variation
        |WHERE variation_pct IS NOT NULL
        |ORDER BY variation_pct DESC
        |LIMIT 10
      """.stripMargin
    ).show()

    // -----------------------------------------------------------------------
    // Requête 4 — Top 10 pires journées
    // -----------------------------------------------------------------------
    println("\n=== Requête 4 : Top 10 pires journées ===")
    spark.sql(
      """
        |SELECT RANK() OVER (ORDER BY variation_pct ASC) AS rang,
        |       Date,
        |       variation_pct
        |FROM aapl_variation
        |WHERE variation_pct IS NOT NULL
        |ORDER BY variation_pct ASC
        |LIMIT 10
      """.stripMargin
    ).show()

    // -----------------------------------------------------------------------
    // Requête 5 — Volume cumulatif par année (2014)
    // -----------------------------------------------------------------------
    println("\n=== Requête 5 : Volume cumulatif sur l'année 2014 ===")
    spark.sql(
      """
        |SELECT Date,
        |       CAST(Volume AS BIGINT) AS Volume,
        |       CAST(
        |         SUM(Volume) OVER (
        |           PARTITION BY SUBSTRING(Date, 1, 4)
        |           ORDER BY Date ASC
        |           ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
        |         )
        |       AS BIGINT) AS volume_cumul_annee
        |FROM aapl
        |WHERE SUBSTRING(Date, 1, 4) = '2014'
        |ORDER BY Date ASC
      """.stripMargin
    ).show(30)

    // -----------------------------------------------------------------------
    // Requête 6 — Creux et sommets annuels
    // -----------------------------------------------------------------------
    println("\n=== Requête 6 : Creux et sommets annuels ===")
    spark.sql(
      """
        |SELECT c.annee,
        |       c.Date          AS date_creux,
        |       ROUND(c.Close, 2) AS prix_creux,
        |       s.Date          AS date_sommet,
        |       ROUND(s.Close, 2) AS prix_sommet
        |FROM (
        |  SELECT SUBSTRING(Date,1,4) AS annee, Date, Close,
        |         RANK() OVER (PARTITION BY SUBSTRING(Date,1,4) ORDER BY Close ASC) AS r
        |  FROM aapl
        |) c
        |JOIN (
        |  SELECT SUBSTRING(Date,1,4) AS annee, Date, Close,
        |         RANK() OVER (PARTITION BY SUBSTRING(Date,1,4) ORDER BY Close DESC) AS r
        |  FROM aapl
        |) s ON c.annee = s.annee AND c.r = 1 AND s.r = 1
        |ORDER BY c.annee ASC
      """.stripMargin
    ).show()

    // -----------------------------------------------------------------------
    // Requête 7 — Golden Cross (MA7 croise MA30 à la hausse)
    // -----------------------------------------------------------------------
    println("\n=== Requête 7 : Golden Cross (MA7 dépasse MA30) ===")

    // Créer une vue avec MA7 et MA30
    spark.sql(
      """
        |SELECT Date, Close,
        |       ROUND(AVG(Close) OVER (
        |         ORDER BY Date ASC ROWS BETWEEN 6 PRECEDING AND CURRENT ROW
        |       ), 2) AS ma7,
        |       ROUND(AVG(Close) OVER (
        |         ORDER BY Date ASC ROWS BETWEEN 29 PRECEDING AND CURRENT ROW
        |       ), 2) AS ma30
        |FROM aapl
      """.stripMargin
    ).createOrReplaceTempView("aapl_ma")

    spark.sql(
      """
        |SELECT Date, Close, ma7, ma30
        |FROM (
        |  SELECT Date, Close, ma7, ma30,
        |         LAG(ma7,  1) OVER (ORDER BY Date ASC) AS ma7_prev,
        |         LAG(ma30, 1) OVER (ORDER BY Date ASC) AS ma30_prev
        |  FROM aapl_ma
        |) t
        |WHERE ma7 > ma30
        |  AND ma7_prev IS NOT NULL
        |  AND ma7_prev <= ma30_prev
        |ORDER BY Date DESC
      """.stripMargin
    ).show(20)

    spark.stop()
  }
}
