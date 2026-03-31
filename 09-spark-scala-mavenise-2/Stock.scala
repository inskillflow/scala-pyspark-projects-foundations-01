// Importation des bibliothèques nécessaires

import org.apache.spark.sql.{SparkSession, DataFrame}  // DataFrame est ici correctement importé
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Encoders


// Définition de la case class pour les stocks
case class Stock(
                  dt: String,
                  openprice: Double,
                  highprice: Double,
                  lowprice: Double,
                  closeprice: Double,
                  volume: Double,
                  adjcloseprice: Double
                )

// Objet contenant les méthodes pour parser et charger les données
object StockProcessor {

  def parseStock(str: String): Option[Stock] = {
    val line = str.split(",")

    try {
      Some(Stock(
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
  def parseRDD(rdd: RDD[String]): RDD[Stock] = {
    val header = rdd.first() // Récupération de l'en-tête
    rdd
      .filter(_ != header) // Suppression de l'en-tête
      .flatMap(parseStock) // Utilisation de flatMap pour ignorer les erreurs
      .cache()
  }
  def main(args: Array[String]): Unit = {
    // Création de la session Spark
    val spark = SparkSession.builder()
      .appName("Stock Analysis")
      .master("local[*]") // Mode local
      .getOrCreate()

    import spark.implicits._ // Import pour convertir RDD en DataFrame

    // Charger le fichier CSV et transformer en DataFrame
    val stocksAAPLDF: DataFrame = parseRDD(spark.sparkContext.textFile("C:/Users/rehou/Downloads/AAPL.csv"))
      .toDF() // Conversion en DataFrame
      .cache()

    // Affichage des premières lignes
    stocksAAPLDF.show()
  }
}
