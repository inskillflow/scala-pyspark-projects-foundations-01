<a id="top"></a>

# Scala — `sealed` et objet companion

## Table des matières

| #  | Section                                                                  |
| -- | ------------------------------------------------------------------------ |
| 1  | [Description du cours](#section-1)                                       |
| 2  | [Vue d’ensemble des deux concepts](#section-2)                           |
| 3  | [Partie 1 — `sealed` en Scala](#section-3)                               |
| 4  | [Pourquoi `sealed` est utile](#section-4)                                |
| 5  | [Exemple 1 — Feux de circulation](#section-5)                            |
| 6  | [Exemple 2 — Moyens de paiement](#section-6)                             |
| 7  | [Exemple 3 — Arbre d’expressions](#section-7)                            |
| 8  | [Différence entre `sealed` et un `trait` normal](#section-8)             |
| 9  | [Différence entre `sealed` et `final`](#section-9)                       |
| 10 | [Partie 2 — Objet companion en Scala](#section-10)                       |
| 11 | [Pourquoi l’objet companion est utile](#section-11)                      |
| 12 | [Exemple 1 — Objet companion simple](#section-12)                        |
| 13 | [Exemple 2 — `apply` pour créer des objets plus facilement](#section-13) |
| 14 | [Exemple 3 — Validation avant création d’objet](#section-14)             |
| 15 | [Exemple 4 — Compteur partagé](#section-15)                              |
| 16 | [Accès aux membres privés](#section-16)                                  |
| 17 | [Tableau comparatif — `sealed` vs objet companion](#section-17)          |
| 18 | [Exemple complet combinant les deux concepts](#section-18)               |
| 19 | [Remarques importantes](#section-19)                                     |
| 20 | [Résumé](#section-20)                                                    |

---

<a id="section-1"></a>

<details>
<summary>1 - Description du cours</summary>

<br/>

Ce cours présente deux concepts très importants en Scala :

* `sealed`
* l’objet companion

Ces deux notions ne servent pas à la même chose :

* `sealed` sert à **contrôler l’héritage et fermer une hiérarchie**
* l’objet companion sert à **associer à une classe une logique utilitaire, de création ou partagée**

Dans les vrais projets Scala, ces deux concepts sont très fréquents et sont souvent utilisés ensemble.

</details>

<p align="right"><a href="#top">↑ Retour en haut</a></p>

---

<a id="section-2"></a>

<details>
<summary>2 - Vue d’ensemble des deux concepts</summary>

<br/>

## `sealed`

Quand on écrit :

```scala
sealed trait Shape
```

on dit :

> tous les sous-types de `Shape` doivent être déclarés dans le même fichier

Cela permet au compilateur Scala de connaître tous les cas possibles.

## Objet companion

Quand on écrit :

```scala
class Person(val name: String)
object Person
```

on crée :

* une classe : `Person`
* un objet : `Person`

Comme ils ont le même nom, ils sont compagnons.

L’objet companion sert généralement à placer :

* des méthodes de création
* des méthodes utilitaires
* des validations
* des constantes
* un état partagé
* une logique comparable au `static` en Java

</details>

<p align="right"><a href="#top">↑ Retour en haut</a></p>

---

<a id="section-3"></a>

<details>
<summary>3 - Partie 1 — <code>sealed</code> en Scala</summary>

<br/>

Le mot-clé `sealed` signifie qu’une hiérarchie est **fermée**.

Exemple :

```scala
sealed trait Animal
```

Cela veut dire que tous les sous-types de `Animal` doivent être définis dans le même fichier.

C’est très utile lorsqu’on connaît à l’avance tous les cas possibles.

Cas d’usage classiques :

* des états
* des rôles
* des résultats
* des catégories
* des arbres d’expressions
* des modèles métiers avec un nombre fini de variantes

</details>

<p align="right"><a href="#top">↑ Retour en haut</a></p>

---

<a id="section-4"></a>

<details>
<summary>4 - Pourquoi <code>sealed</code> est utile</summary>

<br/>

`sealed` est utile pour plusieurs raisons.

### 1. Rendre les `match` plus sûrs

Scala peut vérifier si tous les cas sont bien traités dans un `match`.

### 2. Mieux modéliser le domaine

On exprime clairement qu’un type possède un nombre limité de formes valides.

### 3. Faciliter la maintenance

Le lecteur comprend immédiatement que la hiérarchie est contrôlée et finie.

### 4. Réduire les erreurs cachées

Sans `sealed`, quelqu’un peut ajouter un nouveau sous-type ailleurs et rendre votre `match` incomplet.

</details>

<p align="right"><a href="#top">↑ Retour en haut</a></p>

---

<a id="section-5"></a>

<details>
<summary>5 - Exemple 1 — Feux de circulation</summary>

<br/>

```scala
sealed trait TrafficLight

case object Red extends TrafficLight
case object Yellow extends TrafficLight
case object Green extends TrafficLight
```

Ici :

* `TrafficLight` est le type général
* `Red`, `Yellow` et `Green` sont les seuls cas autorisés dans ce fichier

On peut maintenant utiliser un `match` :

```scala
def action(light: TrafficLight): String = light match {
  case Red    => "Stop"
  case Yellow => "Ralenti"
  case Green  => "Avance"
}
```

### Pourquoi c’est puissant

Scala sait que `TrafficLight` ne peut être que :

* `Red`
* `Yellow`
* `Green`

Si un cas manque :

```scala
def action(light: TrafficLight): String = light match {
  case Red   => "Stop"
  case Green => "Avance"
}
```

le compilateur peut avertir que `Yellow` n’est pas traité.

</details>

<p align="right"><a href="#top">↑ Retour en haut</a></p>

---

<a id="section-6"></a>

<details>
<summary>6 - Exemple 2 — Moyens de paiement</summary>

<br/>

Parfois, les cas transportent des données.

```scala
sealed trait PaymentMethod

case class CreditCard(number: String) extends PaymentMethod
case class PayPal(email: String) extends PaymentMethod
case object Cash extends PaymentMethod
```

On peut ensuite traiter tous les cas :

```scala
def describe(payment: PaymentMethod): String = payment match {
  case CreditCard(number) => s"Paiement par carte : $number"
  case PayPal(email)      => s"Paiement par PayPal : $email"
  case Cash               => "Paiement en espèces"
}
```

Utilisation :

```scala
println(describe(CreditCard("1234-5678")))
println(describe(PayPal("client@mail.com")))
println(describe(Cash))
```

C’est un style de modélisation très fréquent en Scala.

</details>

<p align="right"><a href="#top">↑ Retour en haut</a></p>

---

<a id="section-7"></a>

<details>
<summary>7 - Exemple 3 — Arbre d’expressions</summary>

<br/>

Un exemple classique en programmation fonctionnelle est l’arbre d’expressions.

```scala
sealed trait Expr

case class Number(value: Int) extends Expr
case class Add(left: Expr, right: Expr) extends Expr
case class Multiply(left: Expr, right: Expr) extends Expr
```

Fonction d’évaluation :

```scala
def eval(expr: Expr): Int = expr match {
  case Number(v)      => v
  case Add(a, b)      => eval(a) + eval(b)
  case Multiply(a, b) => eval(a) * eval(b)
}
```

Utilisation :

```scala
val expr = Add(Number(2), Multiply(Number(3), Number(4)))
println(eval(expr))   // 14
```

### Pourquoi cet exemple est important

Il montre que `sealed` est très utile pour modéliser des structures récursives de façon propre et sûre.

</details>

<p align="right"><a href="#top">↑ Retour en haut</a></p>

---

<a id="section-8"></a>

<details>
<summary>8 - Différence entre <code>sealed</code> et un <code>trait</code> normal</summary>

<br/>

Avec un `trait` normal :

```scala
trait Animal
```

quelqu’un peut écrire ailleurs :

```scala
case class Fish(name: String) extends Animal
```

Cela signifie que votre code peut devenir incomplet sans que ce soit visible immédiatement.

Avec un `sealed trait` :

```scala
sealed trait Animal
```

tous les sous-types valides doivent être déclarés dans le même fichier.

La hiérarchie reste donc sous contrôle.

</details>

<p align="right"><a href="#top">↑ Retour en haut</a></p>

---

<a id="section-9"></a>

<details>
<summary>9 - Différence entre <code>sealed</code> et <code>final</code></summary>

<br/>

Ces deux mots-clés sont différents.

## `sealed`

Contrôle la hiérarchie en limitant l’endroit où les sous-classes peuvent être déclarées.

## `final`

Empêche une classe précise d’être étendue.

Exemple :

```scala
sealed trait Vehicle

final case class Car(brand: String) extends Vehicle
final case class Bike(brand: String) extends Vehicle
```

Ici :

* `Vehicle` est une famille fermée
* `Car` et `Bike` ne peuvent pas être sous-classées

Donc :

* `sealed` ferme la famille
* `final` bloque l’héritage d’un type concret

</details>

<p align="right"><a href="#top">↑ Retour en haut</a></p>

---

<a id="section-10"></a>

<details>
<summary>10 - Partie 2 — Objet companion en Scala</summary>

<br/>

Un objet companion est un `object` qui porte le même nom qu’une classe.

Exemple :

```scala
class Person(val name: String, val age: Int)

object Person
```

Ces deux définitions sont liées parce qu’elles partagent le même nom.

L’objet companion sert à stocker une logique liée à la classe elle-même, et non à une instance particulière.

</details>

<p align="right"><a href="#top">↑ Retour en haut</a></p>

---

<a id="section-11"></a>

<details>
<summary>11 - Pourquoi l’objet companion est utile</summary>

<br/>

Un objet companion sert souvent à placer :

* des méthodes de création
* des méthodes utilitaires
* des règles de validation
* des constantes
* des compteurs
* un état partagé
* une logique similaire au `static` en Java

Scala n’utilise pas `static` comme Java.
À la place, on utilise souvent l’objet companion.

</details>

<p align="right"><a href="#top">↑ Retour en haut</a></p>

---

<a id="section-12"></a>

<details>
<summary>12 - Exemple 1 — Objet companion simple</summary>

<br/>

```scala
class Person(val name: String, val age: Int)

object Person {
  def sayHello(): Unit = {
    println("Bonjour depuis l’objet companion Person")
  }
}
```

Utilisation :

```scala
Person.sayHello()
```

Point important :

* `sayHello()` appartient à l’objet `Person`
* cette méthode n’appartient pas à une instance créée avec `new Person(...)`

</details>

<p align="right"><a href="#top">↑ Retour en haut</a></p>

---

<a id="section-13"></a>

<details>
<summary>13 - Exemple 2 — <code>apply</code> pour créer des objets plus facilement</summary>

<br/>

Un usage très fréquent consiste à définir `apply`.

```scala
class Person(val name: String, val age: Int)

object Person {
  def apply(name: String, age: Int): Person = new Person(name, age)
}
```

Utilisation :

```scala
val p = Person("Alice", 25)
```

au lieu de :

```scala
val p = new Person("Alice", 25)
```

C’est l’une des raisons pour lesquelles les objets companion sont très fréquents en Scala.

### Pourquoi `apply` est puissant

Quand un objet définit `apply`, il peut être appelé comme une fonction.

Exemple :

```scala
object MathUtils {
  def apply(x: Int): Int = x * 2
}

println(MathUtils(5))   // 10
```

</details>

<p align="right"><a href="#top">↑ Retour en haut</a></p>

---

<a id="section-14"></a>

<details>
<summary>14 - Exemple 3 — Validation avant création d’objet</summary>

<br/>

Un objet companion peut contrôler la manière dont les objets sont créés.

```scala
class User private (val name: String, val age: Int)

object User {
  def create(name: String, age: Int): Option[User] = {
    if (name.nonEmpty && age >= 0) Some(new User(name, age))
    else None
  }
}
```

Utilisation :

```scala
val u1 = User.create("Alice", 25)
val u2 = User.create("", -3)

println(u1)
println(u2)
```

### Pourquoi c’est utile

Ici :

* le constructeur est `private`
* on ne peut pas créer directement l’objet depuis n’importe où
* seul l’objet companion décide si la création est valide

C’est très utile pour imposer des règles métier.

</details>

<p align="right"><a href="#top">↑ Retour en haut</a></p>

---

<a id="section-15"></a>

<details>
<summary>15 - Exemple 4 — Compteur partagé</summary>

<br/>

Un objet companion peut conserver un état partagé entre toutes les instances.

```scala
class Student(val name: String) {
  val id: Int = Student.nextId()
}

object Student {
  private var currentId: Int = 0

  private def nextId(): Int = {
    currentId += 1
    currentId
  }
}
```

Utilisation :

```scala
val s1 = new Student("Alice")
val s2 = new Student("Bob")

println(s1.id)   // 1
println(s2.id)   // 2
```

Ici, l’objet companion conserve une donnée commune à tous les objets `Student`.

</details>

<p align="right"><a href="#top">↑ Retour en haut</a></p>

---

<a id="section-16"></a>

<details>
<summary>16 - Accès aux membres privés</summary>

<br/>

Une particularité importante est que la classe et son objet companion peuvent accéder à leurs membres privés respectifs.

Exemple :

```scala
class SecretHolder(private val secret: String)

object SecretHolder {
  def reveal(holder: SecretHolder): String = holder.secret
}
```

Utilisation :

```scala
val h = new SecretHolder("Scala est puissant")
println(SecretHolder.reveal(h))
```

Même si `secret` est privé, l’objet companion peut y accéder.

Exemple inverse :

```scala
class Counter {
  def showLastCount(): Int = Counter.lastValue
}

object Counter {
  private var lastValue: Int = 0

  def next(): Int = {
    lastValue += 1
    lastValue
  }
}
```

Utilisation :

```scala
println(Counter.next())   // 1
println(Counter.next())   // 2

val c = new Counter
println(c.showLastCount())   // 2
```

La classe peut elle aussi accéder à un membre privé de son objet companion.

</details>

<p align="right"><a href="#top">↑ Retour en haut</a></p>

---

<a id="section-17"></a>

<details>
<summary>17 - Tableau comparatif — <code>sealed</code> vs objet companion</summary>

<br/>

| Concept         | Rôle principal                    | Utilité                                                | Exemple typique                      |
| --------------- | --------------------------------- | ------------------------------------------------------ | ------------------------------------ |
| `sealed`        | Fermer une hiérarchie             | Sécuriser les `match`, limiter les variantes possibles | `sealed trait Result`                |
| Objet companion | Associer une logique à une classe | Création, validation, utilitaires, état partagé        | `object User { def apply(...) ... }` |

### Idée simple à retenir

* `sealed` contrôle **les formes possibles**
* l’objet companion contrôle **la logique autour d’un type**

</details>

<p align="right"><a href="#top">↑ Retour en haut</a></p>

---

<a id="section-18"></a>

<details>
<summary>18 - Exemple complet combinant les deux concepts</summary>

<br/>

Voici un exemple qui combine `sealed` et objet companion.

```scala
sealed trait OrderStatus

case object Pending extends OrderStatus
case object Paid extends OrderStatus
case object Shipped extends OrderStatus
case object Cancelled extends OrderStatus

class Order private (val id: Int, val status: OrderStatus)

object Order {
  private var currentId: Int = 0

  def create(status: OrderStatus): Order = {
    currentId += 1
    new Order(currentId, status)
  }

  def describe(order: Order): String = order.status match {
    case Pending   => s"Commande ${order.id} en attente"
    case Paid      => s"Commande ${order.id} payée"
    case Shipped   => s"Commande ${order.id} expédiée"
    case Cancelled => s"Commande ${order.id} annulée"
  }
}
```

Utilisation :

```scala
val o1 = Order.create(Pending)
val o2 = Order.create(Paid)

println(Order.describe(o1))
println(Order.describe(o2))
```

### Ce que montre cet exemple

* `sealed` ferme la famille `OrderStatus`
* l’objet companion `Order` gère la création et la logique liée aux commandes
* le `match` sur le statut est sûr, car tous les cas sont connus

</details>

<p align="right"><a href="#top">↑ Retour en haut</a></p>

---

<a id="section-19"></a>

<details>
<summary>19 - Remarques importantes</summary>

<br/>

* `sealed` est souvent utilisé avec `trait`, `case class` et `case object`
* les `case class` possèdent souvent automatiquement un companion object généré par Scala
* un objet companion remplace souvent ce que Java ferait avec `static`
* `sealed` et objet companion ne sont pas concurrents : ils sont complémentaires
* dans un vrai projet Scala, on voit souvent les deux ensemble

</details>

<p align="right"><a href="#top">↑ Retour en haut</a></p>

---

<a id="section-20"></a>

<details>
<summary>20 - Résumé</summary>

<br/>

## `sealed`

`sealed` sert à :

* fermer une hiérarchie
* limiter les sous-types au même fichier
* sécuriser les `match`
* mieux modéliser un ensemble fini de cas

Phrase à retenir :

> `sealed` veut dire : il n’existe que ces cas-là, pas d’autres.

## Objet companion

L’objet companion sert à :

* créer des objets plus proprement
* ajouter des méthodes utilitaires
* centraliser la validation
* stocker un état partagé
* remplacer le `static` de Java

Phrase à retenir :

> l’objet companion représente ce qui est commun, utilitaire ou partagé autour d’une classe.

## Résumé global

* `sealed` contrôle la hiérarchie
* l’objet companion contrôle la logique associée à un type

</details>

<p align="right"><a href="#top">↑ Retour en haut</a></p>

