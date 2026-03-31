# 01 - Données : 

```scala
https://github.com/brahmbhattspandan/ScalaProject/tree/master/data/stocks
```


# 02 - Exemple de données :

```scala
import org.apache.spark.sql.SparkSession

object WordCountExample {
  def main(args: Array[String]): Unit = {

    // Création de la session Spark
    val spark = SparkSession
      .builder()
      .appName("WordCountExample")
      .master("local[*]")
      .getOrCreate()

    // Récupération du SparkContext
    val sc = spark.sparkContext

    // Création d’un RDD à partir d’une liste en mémoire
    val linesRDD = sc.parallelize(List(
      "spark scala spark",
      "scala rdd example",
      "spark makes big data easier"
    ))

    // Découpage en mots
    val wordsRDD = linesRDD.flatMap(line => line.split(" "))

    // Transformation en paires (mot, 1)
    val pairsRDD = wordsRDD.map(word => (word, 1))

    // Agrégation par mot
    val countsRDD = pairsRDD.reduceByKey(_ + _)

    // Affichage du résultat
    countsRDD.collect().foreach(println)

    // Fermeture de Spark
    spark.stop()
  }
}
```

