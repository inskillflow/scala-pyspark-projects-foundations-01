import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

// =============================================================================
//  PARTIE 1 – Analyse des actions AAPL avec Spark SQL
//  (les dates sont conservées en chaîne de caractères)
// =============================================================================

object TP_StockSQL {

  // --------------------------------------------------------------------------
  // Modèle de données
  // --------------------------------------------------------------------------
  case class Stock(
    dt:           String,
    openprice:    Double,
    highprice:    Double,
    lowprice:     Double,
    closeprice:   Double,
    volume:       Double,
    adjcloseprice: Double
  )

  // --------------------------------------------------------------------------
  // Parseurs
  // --------------------------------------------------------------------------
  def parseStock(str: String): Stock = {
    val col = str.split(",")
    Stock(
      col(0),
      col(1).toDouble,
      col(2).toDouble,
      col(3).toDouble,
      col(4).toDouble,
      col(5).toDouble,
      col(6).toDouble
    )
  }

  def parseRDD(rdd: RDD[String]): RDD[Stock] = {
    val header = rdd.first()
    rdd.filter(line => line != header).map(parseStock).cache()
  }

  // --------------------------------------------------------------------------
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("TP Spark SQL – Stocks AAPL")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    import spark.implicits._

    // Chemin vers le fichier CSV (adapter selon votre machine)
    val csvPath = if (args.nonEmpty) args(0)
                  else "data/AAPL.csv"

    // ---------------------------------------------------------------
    // Chargement du DataFrame et renommage de la colonne dt → Date
    // ---------------------------------------------------------------
    val stocksDF = parseRDD(spark.sparkContext.textFile(csvPath))
      .toDF()
      .withColumnRenamed("dt", "Date")
      .cache()

    stocksDF.show()
    stocksDF.printSchema()

    // Enregistrement comme vue SQL temporaire (dates restent en String)
    stocksDF.createOrReplaceTempView("stocks")

    // ============================================================
    // REQUÊTE 1 – Dates de transaction, ouverture et fermeture
    // ============================================================
    println("\n=== Requête 1 : Dates, ouverture et fermeture ===")
    spark.sql(
      """
        |SELECT Date, openprice, closeprice
        |FROM stocks
        |ORDER BY Date
      """.stripMargin
    ).show()

    // ============================================================
    // REQUÊTE 2 – Dates + différence (fermeture – ouverture)
    // ============================================================
    println("\n=== Requête 2 : Dates et différence clôture - ouverture ===")
    spark.sql(
      """
        |SELECT Date,
        |       ROUND(closeprice - openprice, 4) AS diff
        |FROM stocks
        |ORDER BY Date
      """.stripMargin
    ).show()

    // ============================================================
    // REQUÊTE 3 – Max et Min des volumes
    // ============================================================
    println("\n=== Requête 3 : Max et Min des volumes ===")
    spark.sql(
      """
        |SELECT MAX(volume) AS max_volume,
        |       MIN(volume) AS min_volume
        |FROM stocks
      """.stripMargin
    ).show()

    // ============================================================
    // REQUÊTE 4 – Moyenne des valeurs d'ouverture par année
    //  (la date est une chaîne "yyyy-MM-dd" → on extrait les 4 premiers chars)
    // ============================================================
    println("\n=== Requête 4 : Moyenne du prix d'ouverture par année ===")
    spark.sql(
      """
        |SELECT SUBSTRING(Date, 1, 4)          AS annee,
        |       ROUND(AVG(openprice), 4)        AS moyenne_ouverture
        |FROM stocks
        |GROUP BY SUBSTRING(Date, 1, 4)
        |ORDER BY annee
      """.stripMargin
    ).show()

    // ============================================================
    // REQUÊTE 5 – Somme des volumes par mois
    //  (extrait les chars 6-7 de la date)
    // ============================================================
    println("\n=== Requête 5 : Somme des volumes par mois ===")
    spark.sql(
      """
        |SELECT SUBSTRING(Date, 6, 2)           AS mois,
        |       CAST(SUM(volume) AS BIGINT)      AS total_volume
        |FROM stocks
        |GROUP BY SUBSTRING(Date, 6, 2)
        |ORDER BY mois
      """.stripMargin
    ).show()

    spark.stop()
  }
}
