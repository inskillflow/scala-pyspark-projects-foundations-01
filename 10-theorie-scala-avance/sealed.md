# `sealed` en Scala

## 1. C’est quoi `sealed` ?

Le mot-clé `sealed` signifie :

> **on ferme une famille de types**

Quand on écrit :

```scala
sealed trait Animal
```

cela veut dire que tous les types qui héritent de `Animal` doivent être déclarés dans le **même fichier**.

Scala peut donc connaître **tous les cas possibles**.

---

## 2. À quoi ça sert ?

`sealed` sert principalement à 3 choses :

### a) Sécuriser les `match`

Scala peut vérifier si tu as bien traité **tous les cas**.

### b) Modéliser un nombre fini de possibilités

Très utile quand tu sais qu’il n’existe que quelques cas :

* rouge / jaune / vert
* succès / erreur
* carte / cash / PayPal
* chat / chien / oiseau

### c) Rendre le code plus clair

Le lecteur comprend immédiatement que la hiérarchie est **fermée** et contrôlée.

---

# 3. Premier exemple simple

## Exemple : feu de circulation

```scala
sealed trait TrafficLight

case object Red extends TrafficLight
case object Yellow extends TrafficLight
case object Green extends TrafficLight
```

Ici :

* `TrafficLight` est la famille générale
* `Red`, `Yellow`, `Green` sont les seuls cas autorisés dans ce fichier

---

## Utilisation avec `match`

```scala
def action(light: TrafficLight): String = light match {
  case Red    => "Stop"
  case Yellow => "Slow down"
  case Green  => "Go"
}
```

### Pourquoi `sealed` est utile ici ?

Parce que Scala sait que `TrafficLight` ne peut être que :

* `Red`
* `Yellow`
* `Green`

Donc si tu oublies un cas, le compilateur peut t’avertir.

Exemple incomplet :

```scala
def action(light: TrafficLight): String = light match {
  case Red   => "Stop"
  case Green => "Go"
}
```

Ici, il manque `Yellow`.

Avec `sealed`, Scala peut détecter ce problème.

---

# 4. Exemple avec données

Parfois les cas transportent des informations.

## Exemple : moyen de paiement

```scala
sealed trait PaymentMethod

case class CreditCard(number: String) extends PaymentMethod
case class PayPal(email: String) extends PaymentMethod
case object Cash extends PaymentMethod
```

Ici :

* `CreditCard` contient un numéro
* `PayPal` contient un email
* `Cash` ne contient aucune donnée

---

## Traitement

```scala
def describe(payment: PaymentMethod): String = payment match {
  case CreditCard(number) => s"Paiement par carte : $number"
  case PayPal(email)      => s"Paiement PayPal : $email"
  case Cash               => "Paiement en espèces"
}
```

### Exemple d’utilisation

```scala
println(describe(CreditCard("1234-5678")))
println(describe(PayPal("test@mail.com")))
println(describe(Cash))
```

---

# 5. Exemple très pédagogique : animaux

```scala
sealed trait Animal

case class Dog(name: String) extends Animal
case class Cat(name: String) extends Animal
case object Bird extends Animal
```

Fonction :

```scala
def speak(animal: Animal): String = animal match {
  case Dog(name) => s"$name says woof"
  case Cat(name) => s"$name says meow"
  case Bird      => "Bird sings"
}
```

Utilisation :

```scala
println(speak(Dog("Rex")))
println(speak(Cat("Mimi")))
println(speak(Bird))
```

---

# 6. Pourquoi ne pas faire juste un `trait` normal ?

Sans `sealed`, quelqu’un pourrait écrire ailleurs :

```scala
case class Fish(name: String) extends Animal
```

Le problème :
ton `match` précédent ne traite peut-être pas `Fish`.

Donc ton code peut devenir incomplet.

Avec `sealed`, tu gardes le contrôle.

---

# 7. Exemple classique en programmation fonctionnelle

## Représenter un résultat

```scala
sealed trait Result

case class Success(message: String) extends Result
case class Error(code: Int, message: String) extends Result
```

Fonction :

```scala
def show(result: Result): String = result match {
  case Success(message)      => s"Succès : $message"
  case Error(code, message)  => s"Erreur $code : $message"
}
```

### À quoi ça sert ici ?

Très utile pour modéliser :

* succès
* échec
* réponse d’API
* validation
* traitement métier

---

# 8. Exemple style arbre / expression mathématique

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

### Pourquoi c’est puissant ?

Parce qu’on modélise une structure de données complète avec quelques types, puis on la traite proprement avec `match`.

---

# 9. `sealed trait` ou `sealed abstract class` ?

Les deux existent.

## Forme la plus fréquente

```scala
sealed trait Shape
```

## Aussi possible

```scala
sealed abstract class Shape
```

En Scala, on utilise souvent `sealed trait` pour représenter une famille de cas.

---

# 10. Différence avec `final`

Ne pas confondre.

## `sealed`

Ferme la hiérarchie **vers le haut** :
on contrôle qui peut hériter.

## `final`

Bloque l’héritage **sur une classe précise** :
personne ne peut hériter de cette classe.

Exemple :

```scala
sealed trait Vehicle

final case class Car(brand: String) extends Vehicle
final case class Bike(brand: String) extends Vehicle
```

Ici :

* `Vehicle` est une famille fermée
* `Car` et `Bike` ne peuvent pas être sous-classées

---

# 11. Quand utiliser `sealed` ?

Utilise `sealed` quand tu veux représenter :

* des états
* des options
* des résultats
* des catégories fermées
* des événements
* des types métiers bien définis

Exemples :

* `Pending`, `Approved`, `Rejected`
* `Admin`, `Teacher`, `Student`
* `Success`, `Failure`
* `Circle`, `Rectangle`, `Triangle`

---

# 12. Exemple métier réaliste

## Statut d’une commande

```scala
sealed trait OrderStatus

case object Pending extends OrderStatus
case object Paid extends OrderStatus
case object Shipped extends OrderStatus
case object Cancelled extends OrderStatus
```

Fonction :

```scala
def canRefund(status: OrderStatus): Boolean = status match {
  case Pending   => false
  case Paid      => true
  case Shipped   => false
  case Cancelled => false
}
```

Ici, `sealed` sert à garantir que tous les statuts connus sont traités.

---

# 13. Résumé très simple

`sealed` sert à :

* fermer une hiérarchie
* limiter les sous-types au même fichier
* permettre un `match` plus sûr
* mieux modéliser un ensemble fini de cas

---

# 14. Phrase facile à retenir

> `sealed` est utilisé quand on veut dire :
> **“Il n’existe que ces cas-là, pas d’autres.”**

---

# 15. Exemple final complet

```scala
sealed trait UserRole

case object Admin extends UserRole
case object Teacher extends UserRole
case object Student extends UserRole

def permissions(role: UserRole): String = role match {
  case Admin   => "Accès complet"
  case Teacher => "Accès aux cours et notes"
  case Student => "Accès aux contenus"
}

println(permissions(Admin))
println(permissions(Teacher))
println(permissions(Student))
```

---

# 16. Conclusion

`sealed` est très important en Scala parce qu’il aide à écrire un code :

* plus sûr
* plus propre
* plus lisible
* plus facile à maintenir

Il est souvent utilisé avec :

* `trait`
* `case class`
* `case object`
* `match`
