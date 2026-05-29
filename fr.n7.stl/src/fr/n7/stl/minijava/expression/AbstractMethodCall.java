package fr.n7.stl.minijava.expression;

import java.util.Iterator;
import java.util.List;

import fr.n7.stl.minic.ast.expression.Expression;
import fr.n7.stl.minic.ast.expression.FunctionCall;
import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration;
import fr.n7.stl.minijava.ast.type.declaration.MethodDeclaration;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;

/**
 * Classe de base pour tous les appels de méthode dans MiniJava (expression).
 *
 * Un appel de méthode c'est : cible.methode(args).
 * La cible peut être :
 *   - une instance (this, super, une variable de type classe)
 *   - un nom de classe (appel statique, ex: MathUtil.add(10, 20))
 *
 * L'astuce principale : en Java, les méthodes reçoivent "this" en premier paramètre.
 * On construit donc le FunctionCall sous-jacent avec [this, arg1, arg2, ...] dans la liste.
 * Pour les méthodes statiques, on reconstruit le FunctionCall sans "this".
 *
 * Le flag isStaticRebuilt évite de reconstruire le FunctionCall deux fois si on
 * a déjà détecté l'appel statique lors de collectAndPartialResolve.
 */
public abstract class AbstractMethodCall<ObjectKind extends Expression> implements Expression {

    protected String name;

    protected MethodDeclaration declaration;

    protected ObjectKind target;

    protected List<AccessibleExpression> arguments;

    // Le FunctionCall miniC qui délègue la résolution et la génération de code
    protected FunctionCall call;

    // true si on a déjà retiré "this" du FunctionCall (appel statique)
    protected boolean isStaticRebuilt = false;

    /**
     * Constructeur principal.
     *
     * Si target est null, on suppose qu'on est dans une méthode d'instance et que
     * la cible implicite est "this".
     *
     * On construit le FunctionCall avec [target, arg1, arg2, ...] par défaut.
     * Si la cible est un nom de classe (statique), on le reconstruira sans "this"
     * lors de collectAndPartialResolve.
     */
    public AbstractMethodCall(ObjectKind _target, String _name, List<AccessibleExpression> _arguments) {
        this.target = _target;
        if (this.target == null) {
            this.target = (ObjectKind) new fr.n7.stl.minijava.expression.accessible.ThisAccess();
        }
        this.name = _name;
        this.arguments = _arguments;

        java.util.List<AccessibleExpression> allArgs = new java.util.LinkedList<>();
        allArgs.add((AccessibleExpression) this.target);
        allArgs.addAll(this.arguments);

        this.call = new FunctionCall(name, allArgs);
    }

    public AbstractMethodCall(String _name, List<AccessibleExpression> _arguments) {
        this(null, _name, _arguments);
    }

    /**
     * Phase 1 : collect.
     *
     * Si la cible est un nom de classe connu dans le scope (appel statique),
     * on reconstruit le FunctionCall sans "this" pour éviter que miniC
     * essaie de résoudre le nom de classe comme une variable.
     */
    @Override
    public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
        if (this.target != null) {
            String targetName = this.target.toString().trim();
            if (_scope.knows(targetName)) {
                Declaration decl = _scope.get(targetName);
                if (decl instanceof ClassDeclaration) {
                    // Appel statique : on vire "this" du FunctionCall
                    this.call = new FunctionCall(name, this.arguments);
                    this.isStaticRebuilt = true;
                }
            }
        }
        return this.call.collectAndPartialResolve(_scope);
    }

    /**
     * Phase 2 : résolution complète.
     *
     * On détermine la ClassDeclaration cible de deux façons :
     *   1. Si la cible est un nom de classe → appel statique, on prend la classe directement
     *   2. Sinon → on résout la cible comme une expression et on récupère son type
     *
     * Ensuite on cherche la méthode dans la hiérarchie de classes (héritage inclus).
     * On vérifie l'encapsulation (private/protected) via $currentClass dans le scope.
     * Enfin on lie le FunctionCall à la FunctionDeclaration résolue.
     */
    @Override
    public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
        ClassDeclaration classDecl = null;

        // Cas 1 : appel statique via nom de classe (ex: MathUtil.add(...))
        if (this.target != null) {
            String targetName = this.target.toString().trim();
            if (_scope.knows(targetName)) {
                Declaration decl = _scope.get(targetName);
                if (decl instanceof ClassDeclaration) {
                    classDecl = (ClassDeclaration) decl;
                }
            }
        }

        // Cas 2 : appel sur instance, on résout la cible et on récupère son type de classe
        if (classDecl == null) {
            boolean ok = this.target.completeResolve(_scope);
            if (!ok) return false;

            Type targetType = this.target.getType();
            if (!(targetType instanceof ClassType)) {
                Logger.error("L'expression n'est pas un objet. Impossible d'appeler la méthode " + this.name);
                return false;
            }

            classDecl = ((ClassType) targetType).getDeclaration();
        }

        // Recherche de la méthode en remontant la hiérarchie (héritage)
        ClassDeclaration currentDecl = classDecl;
        while (currentDecl != null && this.declaration == null) {
            for (fr.n7.stl.minijava.ast.type.declaration.ClassElement element : currentDecl.getElements()) {
                if (element instanceof MethodDeclaration) {
                    MethodDeclaration md = (MethodDeclaration) element;
                    if (md.getName().equals(this.name)) {
                        this.declaration = md;
                        break;
                    }
                }
            }
            if (this.declaration == null) {
                currentDecl = currentDecl.getAncestorDecl();
            }
        }

        if (this.declaration == null) {
            Logger.error("La méthode " + this.name + " n'existe pas dans la classe " + classDecl.getName());
            return false;
        }

        // Vérification d'encapsulation private/protected
        fr.n7.stl.minijava.ast.type.declaration.AccessRight right = this.declaration.getAccessRight();
        if (right == fr.n7.stl.minijava.ast.type.declaration.AccessRight.PRIVATE
                || right == fr.n7.stl.minijava.ast.type.declaration.AccessRight.PROTECTED) {

            Declaration currentClassDecl = _scope.knows("$currentClass") ? _scope.get("$currentClass") : null;
            String currentClassName = null;
            if (currentClassDecl instanceof fr.n7.stl.minic.ast.instruction.declaration.VariableDeclaration) {
                Type t = ((fr.n7.stl.minic.ast.instruction.declaration.VariableDeclaration) currentClassDecl).getType();
                if (t instanceof ClassType) {
                    currentClassName = ((ClassType) t).getDeclaration().getName();
                }
            }

            if (right == fr.n7.stl.minijava.ast.type.declaration.AccessRight.PRIVATE) {
                if (currentClassName == null || !currentClassName.equals(classDecl.getName())) {
                    Logger.error("Encapsulation error: La méthode " + this.name + " est privée dans la classe " + classDecl.getName() + " et ne peut pas être appelée ici.");
                    return false;
                }
            } else {
                // PROTECTED : OK si on est dans la même classe ou une sous-classe
                boolean isSubclass = false;
                if (currentClassDecl instanceof fr.n7.stl.minic.ast.instruction.declaration.VariableDeclaration) {
                    Type t = ((fr.n7.stl.minic.ast.instruction.declaration.VariableDeclaration) currentClassDecl).getType();
                    if (t instanceof ClassType) {
                        ClassDeclaration curr = ((ClassType) t).getDeclaration();
                        while (curr != null) {
                            if (curr.getName().equals(classDecl.getName())) {
                                isSubclass = true;
                                break;
                            }
                            curr = curr.getAncestorDecl();
                        }
                    }
                }
                if (!isSubclass) {
                    Logger.error("Encapsulation error: La méthode " + this.name + " est protégée dans la classe " + classDecl.getName() + " et ne peut pas être appelée ici.");
                    return false;
                }
            }
        }

        // Appel statique : si le FunctionCall n'a pas encore été reconstruit sans "this", on le fait maintenant
        if (this.declaration.getElementKind() == fr.n7.stl.minijava.ast.type.declaration.ElementKind.CLASS) {
            if (!this.isStaticRebuilt) {
                this.call = new fr.n7.stl.minic.ast.expression.FunctionCall(name, this.arguments);
                this.call.collectAndPartialResolve(_scope);
                this.isStaticRebuilt = true;
            }
        }

        // On lie la FunctionDeclaration résolue au FunctionCall pour la génération de code
        this.call.setFunction(this.declaration.getFunction());
        return this.call.completeResolve(_scope);
    }

    @Override
    public Type getType() {
        return this.declaration.getType();
    }

    @Override
    public String toString() {
        String image = "";
        if (this.target != null) {
            image += this.target + ".";
        }
        image += this.name + "( ";
        Iterator<AccessibleExpression> iterator = this.arguments.iterator();
        if (iterator.hasNext()) {
            AccessibleExpression argument = iterator.next();
            image += argument;
            while (iterator.hasNext()) {
                argument = iterator.next();
                image += " ," + argument;
            }
        }
        image += ")";
        return image;
    }

}
