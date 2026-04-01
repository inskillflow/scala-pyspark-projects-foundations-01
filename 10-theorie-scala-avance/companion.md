# Objet companion en Scala

## 1. C’est quoi ?

Un **companion object** est un `object` qui porte **le même nom** qu’une `class`.

Exemple :

```scala
class Person(val name: String, val age: Int)

object Person
```

Ici :

* `Person` classe
* `Person` objet

Ils ont le même nom, donc ce sont des **companions**.

---

## 2. À quoi ça sert ?

Un companion object sert souvent à :

* créer des objets plus facilement
* définir des méthodes utilitaires liées à une classe
* ajouter une sorte de “statique” comme en Java
* centraliser de la logique de construction
* accéder aux membres privés de la classe compagnon

En Scala, il n’existe pas de mot-clé `static` comme en Java.
On utilise souvent le **companion object** à la place.

---

# 3. Premier exemple simple

```scala
class Person(val name: String, val age: Int)

object Person {
  def sayHello(): Unit = {
    println("Hello from Person companion object")
  }
}
```

Utilisation :

```scala
Person.sayHello()
```

Ici, `sayHello()` appartient à l’objet compagnon, pas à une instance.

---

# 4. Différence entre classe et companion object

## La classe

Elle sert à créer des objets.

```scala
class Person(val name: String, val age: Int)
```

Utilisation :

```scala
val p = new Person("Alice", 25)
```

## Le companion object

Il sert à mettre des méthodes liées à la classe elle-même.

```scala
object Person {
  def defaultPerson(): Person = new Person("Unknown", 0)
}
```

Utilisation :

```scala
val p = Person.defaultPerson()
```

---

# 5. Exemple très classique : factory method

Un usage très fréquent est de créer des objets sans écrire `new` partout.

```scala
class Person(val name: String, val age: Int)

object Person {
  def create(name: String, age: Int): Person = new Person(name, age)
}
```

Utilisation :

```scala
val p = Person.create("Bob", 30)
```

---

# 6. Version plus élégante avec `apply`

En Scala, le companion object contient souvent une méthode `apply`.

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

Au lieu de :

```scala
val p = new Person("Alice", 25)
```

C’est plus propre et très idiomatique en Scala.

---

# 7. Pourquoi `apply` est puissant ?

Parce que ça permet d’appeler l’objet **comme une fonction**.

```scala
object MathUtils {
  def apply(x: Int): Int = x * 2
}

println(MathUtils(5))   // 10
```

Donc avec un companion object, `apply` est souvent utilisé pour construire des instances.

---

# 8. Exemple complet avec affichage

```scala
class Person(val name: String, val age: Int) {
  def introduce(): String = s"My name is $name and I am $age years old."
}

object Person {
  def apply(name: String, age: Int): Person = new Person(name, age)

  def child(name: String): Person = new Person(name, 10)

  def defaultPerson(): Person = new Person("Unknown", 0)
}
```

Utilisation :

```scala
val p1 = Person("Alice", 25)
val p2 = Person.child("Tom")
val p3 = Person.defaultPerson()

println(p1.introduce())
println(p2.introduce())
println(p3.introduce())
```

---

# 9. Companion object = remplacement du `static` de Java

En Java, on ferait :

```java
class Person {
    static void hello() {
        System.out.println("Hello");
    }
}
```

En Scala, on fait plutôt :

```scala
class Person

object Person {
  def hello(): Unit = println("Hello")
}
```

Utilisation :

```scala
Person.hello()
```

Donc :

* Java → `static`
* Scala → `object companion`

---

# 10. Accès aux membres privés

C’est une particularité importante.

La classe et son companion object peuvent accéder à leurs membres privés respectifs.

## Exemple

```scala
class SecretHolder(private val secret: String)

object SecretHolder {
  def reveal(holder: SecretHolder): String = holder.secret
}
```

Utilisation :

```scala
val h = new SecretHolder("Scala is great")
println(SecretHolder.reveal(h))
```

Même si `secret` est privé, l’objet compagnon peut y accéder.

---

# 11. Exemple inverse

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

La classe peut accéder au privé du companion object.

---

# 12. Exemple réaliste : validation avant création

Le companion object est utile quand on veut contrôler la création d’objets.

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

Ici :

* le constructeur est `private`
* seul le companion object peut créer l’objet
* cela permet de valider les données

C’est très puissant.

---

# 13. Exemple avec compteur global

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

Ici, l’objet compagnon garde une donnée partagée par toutes les instances.

---

# 14. Cas fréquent avec `case class`

Les `case class` ont automatiquement un companion object généré par Scala, avec souvent `apply` et `unapply`.

Exemple :

```scala
case class Person(name: String, age: Int)
```

Tu peux écrire :

```scala
val p = Person("Alice", 25)
```

sans écrire `new`, car Scala fournit déjà le companion object.

---

# 15. `unapply` et pattern matching

Le companion object peut aussi contenir `unapply`, utile pour le `match`.

Exemple simplifié :

```scala
class Person(val name: String, val age: Int)

object Person {
  def apply(name: String, age: Int): Person = new Person(name, age)

  def unapply(p: Person): Option[(String, Int)] = Some((p.name, p.age))
}
```

Utilisation :

```scala
val p = Person("Alice", 25)

p match {
  case Person(name, age) => println(s"$name - $age")
}
```

---

# 16. Résumé simple

Un companion object sert à mettre :

* des méthodes de création
* des méthodes utilitaires
* des constantes
* des compteurs
* des validations
* du code “statique”
* de la logique liée à la classe

---

# 17. Phrase facile à retenir

> La classe représente les **objets créés**,
> le companion object représente ce qui est **commun, utilitaire ou statique** autour de cette classe.

---

# 18. Exemple final propre

```scala
class Product(val name: String, val price: Double) {
  def display(): String = s"Product: $name, price: $price"
}

object Product {
  def apply(name: String, price: Double): Product =
    new Product(name, price)

  def freeSample(name: String): Product =
    new Product(name, 0.0)

  def isValidPrice(price: Double): Boolean =
    price >= 0
}
```

Utilisation :

```scala
val p1 = Product("Laptop", 1200.0)
val p2 = Product.freeSample("Sticker")

println(p1.display())
println(p2.display())
println(Product.isValidPrice(50))
println(Product.isValidPrice(-10))
```

---

# 19. Conclusion

L’objet companion en Scala est très important parce qu’il permet de :

* remplacer `static`
* créer des objets proprement
* valider avant construction
* partager des méthodes liées à une classe
* stocker des informations communes

---

## En une phrase

> Un **companion object** est l’objet associé à une classe, utilisé pour contenir la logique commune, les méthodes de création, et le comportement “statique”.

