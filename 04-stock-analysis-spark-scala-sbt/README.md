# TP Spark SQL — BigData2

Travaux pratiques d'analyse de données avec **Apache Spark SQL** en Scala, développés dans le cadre du cours BigData2.

---

## Table des matières

1. [Description du projet](#description-du-projet)
2. [Structure du projet](#structure-du-projet)
3. [Prérequis et versions](#prérequis-et-versions)
4. [Installation et configuration](#installation-et-configuration)
5. [Données CSV](#données-csv)
6. [Partie 1 — Analyse des actions AAPL](#partie-1--analyse-des-actions-aapl)
7. [Partie 2 — Analyse des revenus (Income)](#partie-2--analyse-des-revenus-income)
8. [Résultats obtenus](#résultats-obtenus)
9. [Lancer les programmes](#lancer-les-programmes)
10. [Résolution des problèmes](#résolution-des-problèmes)
11. [Corrections apportées au code original](#corrections-apportées-au-code-original)

---

## Description du projet

Ce projet contient deux programmes Scala/Spark illustrant l'utilisation de **Spark SQL** pour analyser des jeux de données réels :

| Programme | Fichier | Données |
|---|---|---|
| Analyse boursière AAPL | `TP_StockSQL.scala` | Cours journaliers de l'action Apple |
| Analyse des revenus | `TP_Income.scala` | Données démographiques et revenus (UCI Adult Dataset) |

**Points clés :**
- Les requêtes sont exprimées en **SQL pur** via `spark.sql(...)` et des vues temporaires
- Les dates sont conservées en **chaîne de caractères** (`String`) — pas de cast en `DateType`
- Le projet fonctionne en mode local (`local[*]`) sans cluster Spark
- Compatible **Java 21** + **Spark 3.5.1**

---

## Structure du projet

```
projet1-scala/
│
├── build.sbt                          ← Configuration SBT (Spark 3.5.1 / Scala 2.12.18)
│
├── project/
│   ├── build.properties               ← Version SBT : 1.9.7
│   └── plugins.sbt                    ← Plugin sbt-assembly (fat-jar optionnel)
│
├── data/
│   ├── AAPL.csv                       ← Cours boursiers Apple (exemple fourni)
│   └── Income.csv                     ← Données revenus (exemple fourni)
│
├── src/
│   └── main/
│       ├── scala/
│       │   ├── TP_StockSQL.scala      ← PARTIE 1 : analyse des stocks
│       │   └── TP_Income.scala        ← PARTIE 2 : analyse des revenus
│       └── resources/
│
├── .idea/
│   └── runConfigurations/             ← Configurations Run IntelliJ (pré-configurées)
│       ├── TP_StockSQL.xml
│       └── TP_Income.xml
│
└── README.md
```

---

## Prérequis et versions

| Outil | Version | Notes |
|---|---|---|
| **Java JDK** | 21.0.3 | Testé avec Oracle JDK 21 |
| **Scala** | 2.12.18 | Géré automatiquement par SBT |
| **Apache Spark** | 3.5.1 | Première version supportant Java 21 officiellement |
| **SBT** | 1.9.7 | Outil de build |
| **IntelliJ IDEA** | 2025.3+ | avec plugin Scala |
| **OS** | Windows 10/11 | Testé sur Windows 11 |

> **Important :** Spark 3.5.1 est la première version à supporter Java 21 nativement.
> Les versions antérieures (3.3.x, 3.4.x) génèrent des erreurs `NoSuchMethodException` ou `IllegalAccessError` avec Java 17+.

---

## Installation et configuration

### 1. Cloner / ouvrir le projet

```
File → Open → sélectionner le dossier projet1-scala
```

IntelliJ détecte automatiquement le `build.sbt` → cliquer **Trust Project**.

### 2. Attendre le téléchargement des dépendances SBT

SBT télécharge automatiquement Spark 3.5.1 (~500 Mo) depuis Maven Central.
Suivre la progression dans le panneau **SBT** (en bas à droite d'IntelliJ).

### 3. Configurer la variable d'environnement (OBLIGATOIRE avec Java 21)

Spark 3.5.1 a besoin d'accéder à `sun.nio.ch` — un module interne de Java.
Il faut définir la variable d'environnement `JAVA_TOOL_OPTIONS` **une seule fois** :

**Option A — Via PowerShell (recommandée, persistante) :**
```powershell
[System.Environment]::SetEnvironmentVariable(
    "JAVA_TOOL_OPTIONS",
    "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
    "User"
)
```

**Option B — Via les paramètres Windows :**
```
Paramètres → Système → Informations système → Paramètres système avancés
→ Variables d'environnement → Nouvelle variable utilisateur
  Nom  : JAVA_TOOL_OPTIONS
  Valeur : --add-opens=java.base/sun.nio.ch=ALL-UNNAMED
```

**Après avoir défini la variable :**
- Fermer complètement IntelliJ (`File → Exit`)
- Relancer IntelliJ
- Au démarrage, IntelliJ affiche : *"Picked up JAVA_TOOL_OPTIONS: --add-opens=java.base/sun.nio.ch=ALL-UNNAMED"*

### 4. Placer vos fichiers CSV

Copier vos fichiers de données dans le dossier `data/` :

```
data/AAPL.csv       ← cours boursiers Apple
data/Income.csv     ← données démographiques
```

Des fichiers d'exemple sont déjà inclus pour tester.

---

## Données CSV

### AAPL.csv — Format attendu

```
Date,Open,High,Low,Close,Volume,Adj Close
2012-01-03,409.399994,412.5,409,411.230011,75555200,56.880665
2012-01-04,410.0,414.679993,409.28,411.630011,65005500,56.935999
...
```

| Colonne | Type Scala | Description |
|---|---|---|
| `Date` | `String` | Date au format `yyyy-MM-dd` (conservée en String) |
| `openprice` | `Double` | Prix d'ouverture |
| `highprice` | `Double` | Prix le plus haut |
| `lowprice` | `Double` | Prix le plus bas |
| `closeprice` | `Double` | Prix de clôture |
| `volume` | `Double` | Volume de transactions |
| `adjcloseprice` | `Double` | Prix de clôture ajusté |

### Income.csv — Format attendu

```
id,workclass,education,maritalstatus,occupation,relationship,race,gender,nativecountry,income,age,fnlwgt,educationalnum,capitalgain,capitalloss,hoursperweek
1,Private,Bachelors,Never-married,Tech-support,Own-child,White,Male,United-States,<=50K,39,77516,13,2174,0,40
...
```

| Colonne | Type | Description |
|---|---|---|
| `id` | Double | Identifiant |
| `workclass` | String | Catégorie professionnelle |
| `education` | String | Niveau d'éducation |
| `maritalstatus` | String | Statut marital |
| `occupation` | String | Profession |
| `relationship` | String | Relation familiale |
| `race` | String | Origine ethnique |
| `gender` | String | Genre |
| `nativecountry` | String | Pays d'origine |
| `income` | String | Catégorie de revenu : `<=50K` ou `>50K` |
| `age` | Double | Âge |
| `fnlwgt` | Double | Poids final (pondération statistique) |
| `educationalnum` | Double | Nombre d'années d'études |
| `capitalgain` | Double | Gain en capital |
| `capitalloss` | Double | Perte en capital |
| `hoursperweek` | Double | Heures travaillées par semaine |

---

## Partie 1 — Analyse des actions AAPL

**Fichier :** `src/main/scala/TP_StockSQL.scala`

### Architecture du code

```
TP_StockSQL
├── case class Stock(...)         ← modèle de données
├── def parseStock(str)           ← parse une ligne CSV → Stock
├── def parseRDD(rdd)             ← filtre l'en-tête, mappe les lignes
└── def main(args)
    ├── SparkSession.builder()    ← création de la session Spark
    ├── parseRDD(...).toDF()      ← chargement via RDD → DataFrame
    ├── createOrReplaceTempView   ← enregistrement comme vue SQL "stocks"
    └── spark.sql(...)            ← 5 requêtes SQL
```

### Les 5 requêtes Spark SQL

#### Requête 1 — Dates de transaction, ouverture et fermeture

```sql
SELECT Date, openprice, closeprice
FROM stocks
ORDER BY Date
```

Affiche les colonnes `Date` (String), `openprice` et `closeprice` triées chronologiquement.

#### Requête 2 — Différence entre clôture et ouverture

```sql
SELECT Date,
       ROUND(closeprice - openprice, 4) AS diff
FROM stocks
ORDER BY Date
```

Calcule `closeprice - openprice` pour chaque journée. Une valeur positive indique une journée haussière.

#### Requête 3 — Maximum et minimum des volumes

```sql
SELECT MAX(volume) AS max_volume,
       MIN(volume) AS min_volume
FROM stocks
```

Retourne le volume de transactions le plus élevé et le plus faible sur l'ensemble de la période.

#### Requête 4 — Moyenne des prix d'ouverture par année

```sql
SELECT SUBSTRING(Date, 1, 4)       AS annee,
       ROUND(AVG(openprice), 4)    AS moyenne_ouverture
FROM stocks
GROUP BY SUBSTRING(Date, 1, 4)
ORDER BY annee
```

> La date reste en `String` — l'année est extraite avec `SUBSTRING(Date, 1, 4)` au lieu de `YEAR()`.

#### Requête 5 — Somme des volumes par mois

```sql
SELECT SUBSTRING(Date, 6, 2)       AS mois,
       CAST(SUM(volume) AS BIGINT)  AS total_volume
FROM stocks
GROUP BY SUBSTRING(Date, 6, 2)
ORDER BY mois
```

> Le mois est extrait avec `SUBSTRING(Date, 6, 2)` (caractères 6 et 7 de `yyyy-MM-dd`).

---

## Partie 2 — Analyse des revenus (Income)

**Fichier :** `src/main/scala/TP_Income.scala`

Équivalent du travail réalisé avec MapReduce dans l'évaluation 01 BigData2, ici réécrit avec Spark SQL.

### Deux méthodes de chargement

#### Méthode 1 — Lecture CSV avec inferSchema

```scala
val df = spark.read
  .format("csv")
  .option("header", "true")
  .option("inferSchema", "true")    // détection automatique des types
  .load(csvPath)
```

- Avantage : rapide, pas de code supplémentaire
- Types inférés : colonnes numériques → `integer`
- Requête SQL : `SELECT income, ROUND(AVG(age), 2) AS moyenne_age FROM income_view GROUP BY income`

#### Méthode 2 — Lecture via case class + RDD

```scala
case class Income(id: Double, workclass: String, ..., hoursperweek: Double)

def parseIncome(str: String): Income = { ... }

val incomeDF = parseIncomeRDD(sc.textFile(csvPath)).toDF().cache()
```

- Avantage : typage fort, contrôle total du schéma
- Types explicites : colonnes numériques → `double`
- Même requête SQL appliquée sur la vue `income_rdd`

### Comparaison des deux méthodes

| Critère | Méthode 1 (inferSchema) | Méthode 2 (case class) |
|---|---|---|
| Simplicité | Simple | Verbeux |
| Contrôle du schéma | Automatique | Manuel |
| Type `id` | `integer` | `double` |
| Type `age` | `integer` | `double` |
| Performance | Scan du fichier 2× | 1 seul scan |
| Résultat SQL | Identique | Identique |

---

## Sortie console normale au démarrage

Lors de l'exécution depuis IntelliJ, les lignes suivantes s'affichent **systématiquement** au début de la console. Elles sont **toutes normales** et ne représentent pas des erreurs.

### Ligne 1 — Confirmation de la variable d'environnement

```
Picked up JAVA_TOOL_OPTIONS: --add-opens=java.base/sun.nio.ch=ALL-UNNAMED
```

> Cette ligne confirme que la variable `JAVA_TOOL_OPTIONS` a bien été lue par la JVM.
> Elle apparaît à chaque démarrage de Java dès que `JAVA_TOOL_OPTIONS` est définie dans l'environnement.
> C'est **attendu et obligatoire** pour que Spark fonctionne avec Java 21.

### Lignes 2 et suivantes — Initialisation de Spark

```
Using Spark's default log4j profile: org/apache/spark/log4j2-defaults.properties
26/03/31 15:37:20 INFO SparkContext: Running Spark version 3.5.1
26/03/31 15:37:20 INFO SparkContext: OS info Windows 11, 10.0, amd64
26/03/31 15:37:20 INFO SparkContext: Java version 21.0.3
26/03/31 15:37:20 INFO ResourceUtils: ====...====
26/03/31 15:37:20 INFO ResourceUtils: No custom resources configured for spark.driver.
26/03/31 15:37:20 INFO SparkContext: Submitted application: TP Spark SQL – Income
26/03/31 15:37:20 INFO ResourceProfile: Default ResourceProfile created, executor resources: ...
26/03/31 15:37:20 INFO SecurityManager: SecurityManager: authentication disabled; ...
26/03/31 15:37:20 INFO Utils: Successfully started service 'sparkDriver' on port XXXXX.
26/03/31 15:37:21 INFO SparkEnv: Registering MapOutputTracker
26/03/31 15:37:21 INFO SparkEnv: Registering BlockManagerMaster
26/03/31 15:37:21 INFO BlockManagerMasterEndpoint: BlockManagerMasterEndpoint up
26/03/31 15:37:21 INFO DiskBlockManager: Created local directory at C:\...\blockmgr-XXXX
26/03/31 15:37:21 INFO MemoryStore: MemoryStore started with capacity 9.4 GiB
26/03/31 15:37:21 INFO JettyUtils: Start Jetty 0.0.0.0:4040 for SparkUI
26/03/31 15:37:21 WARN Utils: Service 'SparkUI' could not bind on port 4040. Attempting port 4041.
26/03/31 15:37:21 INFO Utils: Successfully started service 'SparkUI' on port 4041.
26/03/31 15:37:21 INFO Executor: Starting executor ID driver on host eleve
26/03/31 15:37:21 INFO BlockManager: Initialized BlockManager: BlockManagerId(driver, eleve, XXXXX, None)
```

Voici ce que signifie chaque groupe de messages :

| Message | Signification | Normal ? |
|---|---|---|
| `Running Spark version 3.5.1` | Version Spark chargée | ✅ |
| `Java version 21.0.3` | Version Java utilisée | ✅ |
| `No custom resources configured` | Pas de GPU/ressources spéciales — mode local standard | ✅ |
| `Submitted application: TP Spark SQL...` | Nom de l'application affiché dans Spark UI | ✅ |
| `SecurityManager: authentication disabled` | Sécurité désactivée — normal en mode local | ✅ |
| `Successfully started service 'sparkDriver' on port XXXXX` | Port réseau interne Spark (varie à chaque exécution) | ✅ |
| `MemoryStore started with capacity 9.4 GiB` | Spark utilise ~9.4 Go de RAM (selon votre machine) | ✅ |
| `SparkUI' could not bind on port 4040. Attempting port 4041` | Port 4040 déjà utilisé (autre Spark actif) → bascule sur 4041 | ✅ WARN inoffensif |
| `Starting executor ID driver on host eleve` | Nom de votre machine (`eleve`) utilisé comme hostname | ✅ |
| `Initialized BlockManager` | Gestionnaire de blocs mémoire prêt | ✅ |

> **Le `WARN` sur le port 4040** apparaît si vous avez lancé deux programmes Spark en même temps
> (ex. `TP_StockSQL` est encore actif quand vous lancez `TP_Income`).
> Ce n'est pas une erreur — Spark utilise simplement le port 4041 à la place.
> Vous pouvez accéder à l'interface web Spark UI à l'adresse : [http://localhost:4041](http://localhost:4041)

### Après l'initialisation — vos résultats

Une fois l'initialisation terminée (environ 2 à 5 secondes), les résultats de vos requêtes SQL s'affichent :

```
=== Méthode 1 : lecture CSV avec inferSchema ===
root
 |-- id: integer (nullable = true)
 ...

=== Méthode 1 : Moyenne d'âge par catégorie de revenu ---
+------+-----------+
|income|moyenne_age|
+------+-----------+
| <=50K|       42.0|
|  >50K|      41.67|
+------+-----------+

Process finished with exit code 0    ← succès
```

> `Process finished with exit code 0` signifie que le programme s'est terminé **sans erreur**.
> Un `exit code 1` indique une erreur.

---

## Résultats obtenus

### TP_StockSQL — Résultats sur AAPL.csv

**Requête 1 — Dates, ouverture et fermeture :**
```
+----------+----------+----------+
|      Date| openprice|closeprice|
+----------+----------+----------+
|2012-01-03|409.399994|411.230011|
|2012-01-04|     410.0|411.630011|
|2013-05-01|     430.0|     442.0|
...
```

**Requête 2 — Différence clôture - ouverture :**
```
+----------+-----+
|      Date| diff|
+----------+-----+
|2012-01-03| 1.83|   ← journée haussière
|2012-01-09|-1.32|   ← journée baissière
|2013-05-01| 12.0|   ← forte hausse
...
```

**Requête 3 — Max et Min des volumes :**
```
+----------+----------+
|max_volume|min_volume|
+----------+----------+
|   8.353E7| 2.72335E7|   ← 83 530 000 et 27 233 500
+----------+----------+
```

**Requête 4 — Moyenne d'ouverture par année :**
```
+-----+-----------------+
|annee|moyenne_ouverture|
+-----+-----------------+
| 2012|         415.3471|
| 2013|           449.47|
| 2014|         310.9775|
| 2015|          123.675|
| 2016|           104.54|
+-----+-----------------+
```

**Requête 5 — Somme des volumes par mois :**
```
+----+------------+
|mois|total_volume|
+----+------------+
|  01|   497187900|
|  02|    91408100|
|  03|   120818500|
|  06|   163552300|
...
```

### TP_Income — Résultats sur Income.csv

**Moyenne d'âge par catégorie de revenu :**
```
+------+-----------+
|income|moyenne_age|
+------+-----------+
| <=50K|       42.0|
|  >50K|      41.67|
+------+-----------+
```

---

## Lancer les programmes

### Depuis IntelliJ IDEA (recommandé)

1. Dans l'arborescence du projet, cliquer droit sur `TP_StockSQL` ou `TP_Income`
2. Sélectionner **Run 'TP_StockSQL'** ou **Run 'TP_Income'**
3. Les résultats s'affichent dans l'onglet **Run** en bas

### Depuis la ligne de commande (sbt-launch.jar)

```powershell
# Avec JAVA_TOOL_OPTIONS déjà défini dans l'environnement
$env:JAVA_TOOL_OPTIONS = "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED"
Set-Location "C:\00-projetsGA\projet1-scala"

# Lancer TP_StockSQL
& "C:\Program Files\Java\jdk-21\bin\java.exe" -jar "C:\00-projetsGA\sbt-launch.jar" "runMain TP_StockSQL"

# Lancer TP_Income
& "C:\Program Files\Java\jdk-21\bin\java.exe" -jar "C:\00-projetsGA\sbt-launch.jar" "runMain TP_Income"
```

### Avec un fichier CSV personnalisé

Passer le chemin en argument :

```powershell
# Avec un fichier CSV externe
& java.exe -jar sbt-launch.jar "runMain TP_StockSQL C:/chemin/vers/mon-fichier.csv"
```

---

## Résolution des problèmes

### Erreur : `NoClassDefFoundError: org/apache/spark/rdd/RDD`

**Cause :** Dépendances Spark marquées `% "provided"` dans `build.sbt`.

**Solution :** Retirer le scope `"provided"` :
```scala
// INCORRECT
"org.apache.spark" %% "spark-core" % sparkVersion % "provided"

// CORRECT
"org.apache.spark" %% "spark-core" % sparkVersion
```

---

### Erreur : `NoSuchMethodException: java.nio.DirectByteBuffer.<init>(long,int)`

**Cause :** Java 17+ bloque l'accès à `DirectByteBuffer` par réflexion. Apparaît avec Spark < 3.5.

**Solution :** Mettre à jour `build.sbt` vers Spark 3.5.1 :
```scala
val sparkVersion = "3.5.1"
```

---

### Erreur : `IllegalAccessError: cannot access class sun.nio.ch.DirectBuffer`

**Cause :** Spark 3.5.1 a encore besoin d'un accès à `sun.nio.ch` non exporté par Java 21.

**Solution :** Définir la variable d'environnement `JAVA_TOOL_OPTIONS` :
```powershell
[System.Environment]::SetEnvironmentVariable("JAVA_TOOL_OPTIONS", "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED", "User")
```
Puis **redémarrer IntelliJ complètement**.

---

### IntelliJ affiche un avertissement sur `JAVA_TOOL_OPTIONS`

**Message :** *"The use of Java options environment variables detected..."*

**Action :** Cliquer **"Don't show again"**. Ce message est informatif uniquement et n'affecte pas l'exécution.

---

### Le programme ne trouve pas le fichier CSV

**Cause :** Le chemin par défaut est `data/AAPL.csv` (relatif au répertoire de travail du projet).

**Solution :** Vérifier que `Working directory` dans la Run Configuration est bien `$PROJECT_DIR$`, ou passer le chemin absolu en argument.

---

## Corrections apportées au code original

Le code original fourni dans le TP contenait plusieurs erreurs qui ont été corrigées :

| # | Problème original | Correction appliquée |
|---|---|---|
| 1 | `import sqlContext.implicits._` | Remplacé par `import spark.implicits._` (API Spark 2+) |
| 2 | `import spark._` et `import sqlContext._` | Supprimé (imports inutiles / conflictuels) |
| 3 | Commentaires `###` (syntaxe Python) | Remplacé par `//` (syntaxe Scala) |
| 4 | `col("Date")cast(DateType)` | Corrigé en `col("Date").cast(DateType)` (point manquant) |
| 5 | `def parseRDD` défini deux fois dans le même scope | Renommé `parseIncomeRDD` pour la partie Income |
| 6 | `val Income = ...` conflicte avec `case class Income` | Renommé `incomeDF` |
| 7 | `line(14).toDouble` répété pour `hoursperweek` | Corrigé en `line(15).toDouble` |
| 8 | `SparkSession` créé mais `sqlContext` utilisé | Tout migré vers l'API `SparkSession` / `spark.sql()` |
| 9 | Dépendances Spark `% "provided"` | Retiré pour permettre l'exécution locale dans IntelliJ |
| 10 | Spark 3.3.2 incompatible avec Java 21 | Mis à jour vers Spark 3.5.1 |
