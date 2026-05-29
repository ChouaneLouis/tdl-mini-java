# 📋 Résumé des Corrections - Compilateur MiniJava

Ce document explique en détail les modifications récentes apportées au compilateur pour faire fonctionner correctement les **accès aux attributs**, les **appels de méthodes** et la **gestion de la mémoire**. À partager avec l'équipe pour bien comprendre ce qui posait problème et comment ça a été résolu.

---

## 1. Appels de Méthodes (Réutilisation de MiniC)

Le problème principal était que le compilateur ne gérait pas correctement les appels de méthodes (plantages lors de la résolution, pas de code TAM généré). L'idée a été de réutiliser à 100% la classe `FunctionCall` de MiniC.

*   **Fichiers modifiés :** `MethodCall.java` (Instruction) et `AbstractMethodCall.java` (Expression).
*   **La ruse du "this" caché :** Une méthode en MiniJava est juste une fonction MiniC avec un paramètre caché `this` en première position. Dans les constructeurs de `MethodCall` et `AbstractMethodCall`, on instancie maintenant un `FunctionCall` de MiniC, en lui injectant la cible (`target`, l'objet appelant) comme **tout premier argument**. (Si la cible est implicite, on injecte un `new ThisAccess()`).
*   **Résolution :** Les appels à `collectAndPartialResolve` et `completeResolve` sont désormais simplement délégués au `FunctionCall` interne, ce qui garantit que tous les arguments sont bien résolus (corrige les `NullPointerException`).
*   **Génération de code (TAM) :** 
    *   Pour `MethodCallAccess` (Expression), on retourne juste le code du `FunctionCall`.
    *   Pour `MethodCall` (Instruction), on appelle la méthode pour son effet de bord (ex: `p.setAge(5);`). On ajoute donc un `POP` après le `CALL` pour supprimer la valeur de retour de la pile afin d'éviter une fuite de mémoire.

---

## 2. Le bug vicieux de la mémoire (Offsets des paramètres)

Il y avait un bug majeur qui faussait les adresses mémoire des paramètres de méthodes (comme `-1[LB]`). 

*   **Fichier modifié :** `ClassDeclaration.java`
    *   **Le problème :** La méthode `allocateMemory` parcourait les éléments de la classe pour calculer la taille des attributs, mais **elle n'appelait jamais `allocateMemory` sur ses méthodes et ses constructeurs**. Conséquence : les paramètres des méthodes gardaient des offsets "fantômes" (par défaut à -1) et la machine TAM lisait au mauvais endroit en mémoire.
    *   **La solution :** Ajout des appels `md.getFunction().allocateMemory(Register.LB, 3)` et `cd.allocateMemory(Register.LB, 3)` dans le `for` de la déclaration de classe.

*   **Fichier modifié :** `ConstructorDeclaration.java`
    *   **La solution :** Ajout complet de la méthode `allocateMemory` (qui n'existait pas) pour calculer et attribuer des offsets négatifs aux paramètres du constructeur, comme ça se fait pour les fonctions classiques.

*   **Fichier modifié :** `ParameterDeclaration.java`
    *   Ajout d'un setter `setOffset(int)` pour permettre aux constructeurs de modifier l'offset du paramètre.

*   **Fichier modifié :** `ParameterAccess.java` (Le "Hack" supprimé)
    *   **Le problème :** Comme le calcul mémoire ne marchait pas, quelqu'un avait tenté de réparer ça avec un "hack" en modifiant le code de chargement : `offset - 1`. 
    *   **La solution :** Vu que les offsets sont maintenant parfaitement calculés, ce "-1" faisait planter les appels. Le hack a été supprimé pour revenir à `offset` pur.

---

## 3. Allocation d'Objet (MAlloc)

*   **Fichier modifié :** `ObjectAllocation.java`
    *   **Le problème :** Si on crée un objet issu d'une classe sans attributs (Taille = 0), le compilateur générait `LOADL 0` puis `SUBR MAlloc`. Allouer 0 mot pose problème sur la machine virtuelle.
    *   **La solution :** Forcer l'allocation d'au moins 1 mot en mémoire. Changement du code par : `int objectSize = Math.max(1, classDecl.getObjectSize());`.

---

## 4. Accès et Modification d'Attributs (Session précédente)

*   **Fichiers modifiés :** `AttributeAccess.java`, `AttributeAssignment.java`
    *   Implémentation de la fonction `getCode` : on évalue la cible (`target`), puis on ajoute l'offset de l'attribut à l'adresse de base (avec `IAdd`), puis on lit (`LOADI (1)`) ou on écrit (`STOREI (1)`).
*   **Fichier modifié :** `ThisAccess.java`, `ThisAssignment.java`
    *   Suppression des calculs mathématiques bizarres (`-1 * -1 = +1`). Maintenant, le mot-clé `this` charge simplement l'adresse mémoire passée en premier paramètre (`LOAD (1) -1[LB]`).

---

## 5. Méthodes Statiques (`static`)

Pour qu'une méthode `static` fonctionne correctement, elle ne doit **pas** utiliser l'objet appelant (`this`) comme paramètre, car elle appartient à la classe et non à l'instance.

*   **Fichier de test :** J'ai créé `tests/ok_java/test_static.mjava` pour vérifier le bon appel d'une méthode `static`.
*   **Fichier modifié :** `ClassDeclaration.java`
    *   **Le problème :** Lors de la déclaration des paramètres, le compilateur ajoutait automatiquement le paramètre `this` à **toutes** les méthodes.
    *   **La solution :** Ajout d'une condition : `if (md.getElementKind() != ElementKind.CLASS) { md.parameters.add(0, thisDeclaration); }`. Si la méthode est statique (`ElementKind.CLASS`), on n'ajoute plus `this` !
*   **Fichiers modifiés :** `MethodCall.java` et `AbstractMethodCall.java`
    *   **Le problème :** Quand on instancie `FunctionCall`, on lui passe `this.target` (l'objet qui appelle). Mais pour une méthode statique, il ne faut pas l'envoyer.
    *   **La solution :** Dans `completeResolve`, une fois qu'on a trouvé la méthode et vérifié qu'elle est statique (`ElementKind.CLASS`), on recrée un nouveau `FunctionCall` en lui passant **uniquement** les arguments de l'utilisateur (`this.arguments`), sans la cible.
*   **Fichier modifié :** `MethodDeclaration.java`
    *   Correction de la fonction `toString()` pour qu'elle affiche bien le mot-clé `"static "` si la méthode est statique, pour que l'arbre AST généré (et le débug) soit lisible.

**Bilan :** Le compilateur gère désormais l'héritage de base (grâce à MiniC), les appels de méthodes avec paramètres, la modification d'attributs via `this`, les méthodes statiques (sans instanciation du `this`), et la pile TAM reste propre (grâce aux bons `POP` et `RETURN` avec dépilement).

---

## 6. Classes et Méthodes Abstraites (`abstract`)

Le mot-clé `abstract` signifie qu'une méthode n'a pas de corps (pas de code), et qu'une classe ne peut pas être instanciée (on ne peut pas faire un `new` dessus).

*   **Fichier de test :** J'ai créé `tests/ko_java/test_abstract.mjava` pour vérifier qu'on ne peut plus instancier de classe abstraite sans que le compilateur ne plante salement.
*   **Fichier modifié :** `ClassDeclaration.java`
    *   **Le problème :** Lors de la génération de code (`getCode`), le compilateur essayait d'appeler `md.body.getCode(_factory)` sur les méthodes abstraites. Comme `body` est `null`, ça provoquait une belle `NullPointerException` qui faisait tout crasher.
    *   **La solution :** J'ai rajouté une vérification `if (md.isConcrete())` autour de la génération du code de la méthode, ainsi que la création du getter `isConcrete()`. J'ai aussi ajouté un getter `isConcrete()` dans `MethodDeclaration.java`.
*   **Fichier modifié :** `MethodDeclaration.java`
    *   **Le problème :** Pareil que pour la génération de code, la phase de résolution plantait car elle essayait de résoudre les paramètres dans le `body` qui était `null`.
    *   **La solution :** Ajout de la vérification `if (this.concrete)` dans `collectAndPartialResolve` et `completeResolve` avant d'appeler les méthodes sur `body`.
*   **Fichier modifié :** `ObjectAllocation.java`
    *   **Le problème :** MiniJava autorisait la création d'objet à partir de classes abstraites (`new Shape()`).
    *   **La solution :** Dans `completeResolve`, j'ai rajouté une vérification : si la déclaration de la classe liée à ce type n'est pas "concrete" (`!classDecl.isConcrete()`), on lève une belle erreur `Logger.error("Erreur : Impossible d'instancier la classe abstraite")` et on renvoie `false`.
*   **Fichier modifié :** `ClassDeclaration.java` (Correction bonus pour l'héritage)
    *   Le compilateur crashait avec "déjà connu" si une classe enfant (ex: `Square`) redéfinissait la méthode de la classe parente (ex: `area()`). J'ai modifié l'enregistrement dans le scope global pour que cette situation soit ignorée silencieusement au lieu de tout faire exploser.
