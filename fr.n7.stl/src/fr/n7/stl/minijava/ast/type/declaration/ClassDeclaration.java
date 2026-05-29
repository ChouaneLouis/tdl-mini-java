package fr.n7.stl.minijava.ast.type.declaration;

import java.util.List;

import fr.n7.stl.minic.ast.instruction.Instruction;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.scope.SymbolTable;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;

/**
 * Représente la déclaration d'une classe MiniJava dans l'AST.
 *
 * Elle implémente à la fois Instruction (pour pouvoir être placée dans un bloc)
 * et Declaration (pour être enregistrée dans un scope et retrouvée par son nom).
 *
 * Les classes sont soit concrètes, soit abstraites (comme en Java).
 * L'héritage est géré via un nom d'ancêtre résolu en ClassDeclaration lors de completeResolve.
 */
public class ClassDeclaration implements Instruction, Declaration {

    // Liste de tous les éléments de la classe : attributs, méthodes, constructeurs
    protected List<ClassElement> elements;

    public List<ClassElement> getElements() {
        return elements;
    }

    // true = classe concrète, false = classe abstraite (abstract)
    protected boolean concrete;

    public boolean isConcrete() {
        return this.concrete;
    }

    protected String name;

    // Nom textuel de la classe parente (null si pas d'héritage)
    protected String ancestor;
    // La déclaration résolue de la classe parente (remplie lors de completeResolve)
    protected ClassDeclaration ancestorDecl;

    public String getAncestor() {
        return this.ancestor;
    }

    public ClassDeclaration getAncestorDecl() {
        return this.ancestorDecl;
    }

    // Scope propre à cette classe, utilisé pour la résolution des membres
    private SymbolTable classScope;

    /**
     * Constructeur principal.
     *
     * On injecte automatiquement un constructeur sans paramètre si la classe n'en
     * a pas. C'est le comportement de Java : toute classe a au moins un constructeur.
     *
     * On règle aussi le préfixe "Method_NomClasse_" sur chaque méthode ici,
     * pour que les labels TAM soient uniques (ex : Method_Animal_speak).
     */
    public ClassDeclaration(boolean _concrete, String _name, String _ancestor, List<ClassElement> _elements) {
        this.concrete = _concrete;
        this.name = _name;
        this.ancestor = _ancestor;
        this.elements = _elements;

        boolean hasConstructor = false;
        for (ClassElement el : this.elements) {
            if (el instanceof ConstructorDeclaration) {
                hasConstructor = true;
            }
            if (el instanceof MethodDeclaration) {
                // On fixe le nom complet de la méthode maintenant qu'on connaît la classe
                ((MethodDeclaration) el).setClassName(this.name);
            }
        }

        // Si la classe n'a pas de constructeur explicite, on en injecte un vide
        // (comportement identique à Java qui génère un constructeur par défaut)
        if (!hasConstructor && !this.name.equals("Main")) {
            ConstructorDeclaration defaultCons = new ConstructorDeclaration(
                this.name,
                new java.util.LinkedList<>(),
                new fr.n7.stl.minic.ast.Block(new java.util.LinkedList<>())
            );
            this.elements.add(defaultCons);
        }
    }

    /** Constructeur sans ancêtre (classe qui n'hérite pas). */
    public ClassDeclaration(boolean _concrete, String _name, List<ClassElement> _elements) {
        this(_concrete, _name, null, _elements);
    }

    /**
     * Phase 1 : collecte et résolution partielle.
     *
     * On enregistre la classe dans le scope global, puis on crée un scope local
     * pour ses membres. Chaque méthode/constructeur reçoit le paramètre implicite
     * "this" ici, avant d'être traité.
     *
     * Règle importante :
     * - Pour les constructeurs, "this" est ajouté EN DERNIER dans la liste des params
     *   (offset -1[LB]) car ObjectAllocation l'empile après les autres arguments.
     * - Pour les méthodes classiques, "this" est inséré EN PREMIER (index 0),
     *   donc il est au fond de la pile des paramètres.
     * - Pour les méthodes statiques (ElementKind.CLASS), on ne met pas "this".
     *
     * On injecte aussi $currentClass dans le scope local pour que les contrôles
     * d'encapsulation (private/protected) sachent dans quelle classe on compile.
     */
    @Override
    public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
        if (!_scope.accepts(this)) {
            Logger.error("Erreur de collecte : La classe " + this.name + " est déjà définie.");
            return false;
        }

        _scope.register(this);

        this.classScope = new SymbolTable(_scope);
        // Marqueur de classe courante, utilisé lors des vérifications d'accès private/protected
        this.classScope.register(new fr.n7.stl.minic.ast.instruction.declaration.VariableDeclaration("$currentClass", getType(), null));

        boolean isValid = true;

        for (ClassElement classElement : this.elements) {

            if (classElement instanceof ConstructorDeclaration) {
                ConstructorDeclaration cd = (ConstructorDeclaration) classElement;
                // "this" en dernière position → offset -1[LB] dans le cadre de pile
                cd.parameters.add(new ParameterDeclaration("this", getType()));
                isValid = isValid && cd.collectAndPartialResolve(this.classScope);

            } else if (classElement instanceof AttributeDeclaration) {
                AttributeDeclaration ad = (AttributeDeclaration) classElement;
                // On interdit les doublons dans la même classe (le shadowing est géré par le scope parent)
                if (!this.classScope.accepts(ad)) {
                    Logger.error(ad.name + " déjà défini");
                    return false;
                }
                this.classScope.register(ad);

            } else if (classElement instanceof MethodDeclaration) {
                MethodDeclaration md = (MethodDeclaration) classElement;

                // On enregistre les méthodes publiques dans le scope global pour qu'elles
                // soient trouvables via le scope lors d'un appel simple (sans préfixe de classe).
                // Si un nom est déjà pris (override, même nom ailleurs), on l'ignore silencieusement.
                if (md.getAccessRight() == AccessRight.PUBLIC) {
                    if (_scope.accepts(md)) {
                        _scope.register(md);
                    }
                }

                // "this" en première position pour les méthodes d'instance → offset négatif le plus bas
                if (md.getElementKind() != ElementKind.CLASS) {
                    md.parameters.add(0, new ParameterDeclaration("this", getType()));
                }

                isValid = isValid && md.collectAndPartialResolve(this.classScope);

            } else {
                Logger.error(classElement.name + " n'est pas du bon type");
            }
        }

        return isValid;
    }

    @Override
    public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
        return this.collectAndPartialResolve(_scope);
    }

    /**
     * Phase 2 : résolution complète.
     *
     * On résout le nom de l'ancêtre en ClassDeclaration (si la classe hérite de quelque chose),
     * puis on propage la résolution à chaque membre.
     */
    @Override
    public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
        boolean isValid = true;

        if (this.ancestor != null) {
            if (!_scope.knows(this.ancestor)) {
                Logger.error("La classe mère '" + this.ancestor + "' de " + this.name + " n'existe pas.");
                isValid = false;
            } else {
                Declaration decl = _scope.get(this.ancestor);
                if (decl instanceof ClassDeclaration) {
                    this.ancestorDecl = (ClassDeclaration) decl;
                } else {
                    Logger.error("L'ancêtre '" + this.ancestor + "' n'est pas une classe.");
                    isValid = false;
                }
            }
        }

        for (ClassElement classElement : this.elements) {
            if (classElement instanceof ConstructorDeclaration) {
                ConstructorDeclaration cd = (ConstructorDeclaration) classElement;
                isValid = isValid && cd.completeResolve(this.classScope);
            } else if (classElement instanceof AttributeDeclaration) {
                AttributeDeclaration attribute = (AttributeDeclaration) classElement;
                isValid = isValid && attribute.getType().completeResolve(this.classScope);
            } else if (classElement instanceof MethodDeclaration) {
                MethodDeclaration md = (MethodDeclaration) classElement;
                isValid = isValid && md.completeResolve(this.classScope);
            } else {
                Logger.error(classElement.name + " n'est pas du bon type");
            }
        }

        return isValid;
    }

    @Override
    public boolean checkType() {
        boolean ok = true;
        for (ClassElement classElement : elements) {
            if (classElement instanceof Instruction)
                ok = ok && ((Instruction) classElement).checkType();
        }
        return ok;
    }

    /**
     * Calcul des offsets mémoire pour les attributs et allocation mémoire
     * des méthodes/constructeurs.
     *
     * Les attributs sont placés les uns après les autres dans l'objet.
     * Si la classe hérite d'une autre, on commence à l'offset de fin de l'ancêtre
     * (les attributs hérités occupent déjà les premiers slots).
     */
    @Override
    public int allocateMemory(Register _register, int _offset) {
        // On part de la taille de l'ancêtre pour ne pas écraser ses attributs
        int localOffset = (this.ancestorDecl != null) ? this.ancestorDecl.getObjectSize() : 0;

        for (ClassElement classElement : elements) {
            if (classElement instanceof AttributeDeclaration) {
                AttributeDeclaration atr = (AttributeDeclaration) classElement;
                atr.offset = localOffset;
                localOffset += atr.getType().length();
            } else if (classElement instanceof MethodDeclaration) {
                MethodDeclaration md = (MethodDeclaration) classElement;
                if (md.isConcrete()) {
                    md.getFunction().allocateMemory(Register.LB, 3);
                }
            } else if (classElement instanceof ConstructorDeclaration) {
                ConstructorDeclaration cd = (ConstructorDeclaration) classElement;
                cd.allocateMemory(Register.LB, 3);
            }
        }
        return _offset; // Une déclaration de classe n'occupe pas d'espace dans le bloc courant
    }

    /**
     * Génération du code TAM pour la classe entière.
     *
     * On génère un JUMP pour sauter par-dessus le code des méthodes/constructeurs
     * (ils ne doivent pas être exécutés séquentiellement, seulement via CALL).
     * Les attributs ne génèrent pas de code ici, ils font partie de l'objet alloué sur le tas.
     *
     * Pour chaque méthode concrète, on génère son corps + un RETURN de sécurité à la fin.
     * Ce RETURN est là pour les méthodes void : sans lui, le programme saute dans le vide.
     */
    @Override
    public Fragment getCode(TAMFactory _factory) {
        Fragment fragment = _factory.createFragment();
        String skipLabel = "skip_" + this.name;
        fragment.add(_factory.createJump(skipLabel));

        for (ClassElement classElement : this.elements) {

            if (classElement instanceof ConstructorDeclaration) {
                ConstructorDeclaration cd = (ConstructorDeclaration) classElement;
                fragment.append(cd.getCode(_factory));

            } else if (classElement instanceof AttributeDeclaration) {
                // Les attributs n'ont pas de code propre, ils sont initialisés dans le constructeur
                continue;

            } else if (classElement instanceof MethodDeclaration) {
                MethodDeclaration md = (MethodDeclaration) classElement;
                if (md.isConcrete()) {
                    Fragment methode = md.body.getCode(_factory);
                    methode.addPrefix("Method_" + this.name + "_" + md.getName());

                    // Taille totale des paramètres (this inclus) pour le RETURN final
                    int paramSize = 0;
                    for (fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration p : md.getParameters()) {
                        paramSize += p.getType().length();
                    }
                    // RETURN de sécurité : indispensable pour les méthodes void qui n'ont pas de return explicite
                    methode.add(_factory.createReturn(0, paramSize));
                    fragment.append(methode);
                }
            } else {
                Logger.error(classElement.name + " n'est pas du bon type");
            }
        }
        fragment.addSuffix(skipLabel);

        return fragment;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Type getType() {
        // Un ClassType est un type "pointeur vers objet", de taille 1 (adresse)
        return new ClassType(this);
    }

    @Override
    public String toString() {
        String image = "";
        if (!this.concrete) {
            image += "abstract ";
        }
        image += "class " + this.name + " ";
        if (this.ancestor != null) {
            image += "extends " + this.ancestor + " ";
        }
        image += "{\n";
        for (ClassElement e : this.elements) {
            image += e;
        }
        image += "}\n";
        return image;
    }

    /**
     * Renvoie la taille totale d'un objet de cette classe en mémoire TAM.
     * Ça inclut les attributs hérités (taille de l'ancêtre) + les attributs propres.
     * Utilisé pour le LOADL avant MAlloc dans ObjectAllocation.
     */
    public int getObjectSize() {
        int size = (this.ancestorDecl != null) ? this.ancestorDecl.getObjectSize() : 0;
        for (ClassElement element : this.elements) {
            if (element instanceof AttributeDeclaration) {
                size += ((AttributeDeclaration) element).getType().length();
            }
        }
        return size;
    }

}
