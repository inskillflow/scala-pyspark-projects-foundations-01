import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.Encoders

// Définition de la case class pour les stocks
case class Stock1(
                   dt: String,
                   openprice: Double,
                   highprice: Double,
                   lowprice: Double,
                   closeprice: Double,
                   volume: Double,
                   adjcloseprice: Double
                 )

object StockProcessor1 {

  // Fonction pour parser une ligne CSV en objet Stock
  def parseStock(str: String): Option[Stock1] = {
    val line = str.split(",")

    try {
      Some(Stock1(
        line(0),
        line(1).toDouble,
        line(2).toDouble,
        line(3).toDouble,
        line(4).toDouble,
        line(5).toDouble,
        line(6).toDouble
      ))
    } catch {
      case e: Exception =>
        println(s"Erreur de parsing pour la ligne : $str -> ${e.getMessage}")
        None
    }
  }

  // Fonction pour transformer un RDD brut en RDD d'objets Stock
  def parseRDD(rdd: RDD[String]): RDD[Stock1] = {
    val header = rdd.first() // Récupération de l'en-tête
    rdd
      .filter(_ != header) // Suppression de l'en-tête
      .flatMap(parseStock) // Transformation avec gestion des erreurs
      .cache()
  }

  def main(args: Array[String]): Unit = {
    // Création de la session Spark
    val spark = SparkSession.builder()
      .appName("Stock Analysis")
      .master("local[*]") // Mode local avec plusieurs threads
      .getOrCreate()

    import spark.implicits._ // Import pour convertir RDD en DataFrame

    // Définition du chemin d'accès au fichier (format compatible avec Spark)
    val filePath = "file:///C:/Users/rehou/Downloads/AAPL.csv"

    // Vérification de l'existence du fichier avant de le charger
    val sc = spark.sparkContext
    val fileExists = sc.hadoopConfiguration.get("fs.defaultFS") == "file:///" || new java.io.File("C:/Users/rehou/Downloads/AAPL.csv").exists()

    if (!fileExists) {
      println(s"Erreur: Le fichier CSV n'existe pas à l'emplacement $filePath")
      spark.stop()
      return
    }

    // Charger le fichier CSV et transformer en DataFrame
    val stockDF: DataFrame = parseRDD(sc.textFile(filePath))
      .toDF("dt", "openprice", "highprice", "lowprice", "closeprice", "volume", "adjcloseprice") // Ajout des noms de colonnes explicites
      .cache()

    // Ajouter une colonne de moyenne mobile sur 5 jours
    val stockWithMovingAvgDF = stockDF
      .withColumn("moving_avg_5", avg(col("closeprice"))
        .over(Window.orderBy("dt").rowsBetween(-4, 0))) // Moyenne des 5 derniers jours

    // Répartition en plusieurs partitions
    val partitionedStockDF = stockWithMovingAvgDF.repartition(10) // Diviser en 5 partitions

    // Sauvegarde en Parquet pour performance optimisée
    val outputPath = "file:///C:/Users/rehou/Downloads/sr"
    partitionedStockDF.write.mode("overwrite").csv(outputPath)

    // Afficher un aperçu des données
    partitionedStockDF.show(10)

    // Fermeture de la session Spark
    spark.stop()
  }
}