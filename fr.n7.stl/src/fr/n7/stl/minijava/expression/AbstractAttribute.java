package fr.n7.stl.minijava.expression;

import fr.n7.stl.minic.ast.expression.Expression;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.minijava.ast.type.declaration.AttributeDeclaration;
import fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;

/**
 * Classe de base pour l'accès et l'affectation d'un attribut d'objet.
 *
 * Gère les deux cas : lecture (AttributeAccess) et écriture (AttributeAssignment).
 * La résolution cherche l'attribut dans la classe de l'objet ET dans toute sa hiérarchie
 * (on remonte les ancêtres jusqu'à trouver l'attribut ou échouer).
 *
 * La classe qui déclare l'attribut (declaringClass) est mémorisée pour les vérifications
 * d'encapsulation. Important pour le private hérité : un attribut private de Parent
 * n'est pas accessible depuis Child, même si Child hérite de Parent.
 */
public abstract class AbstractAttribute<ObjectKind extends Expression> implements Expression {

    protected ObjectKind object;
    protected String name;
    // La déclaration de l'attribut, remplie lors de completeResolve
    protected AttributeDeclaration attribute;

    public AbstractAttribute(ObjectKind _object, String _name) {
        this.object = _object;
        this.name = _name;
    }

    @Override
    public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
        // On propage juste vers l'objet cible
        return object.collectAndPartialResolve(_scope);
    }

    /**
     * Résolution complète de l'accès à l'attribut.
     *
     * 1. On résout l'objet (ex: "this", "a", "super")
     * 2. On vérifie que son type est bien une classe
     * 3. On cherche l'attribut dans la classe et ses ancêtres
     * 4. On vérifie l'encapsulation (private = seulement dans la classe déclarante,
     *    protected = dans la classe déclarante ou ses sous-classes)
     */
    @Override
    public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
        boolean ok = this.object.completeResolve(_scope);
        if (!ok) return false;

        Type objectType = this.object.getType();
        if (!(objectType instanceof ClassType)) {
            Logger.error("L'expression " + this.object + " n'est pas un objet. Impossible d'accéder à l'attribut " + this.name);
            return false;
        }

        ClassType classType = (ClassType) objectType;
        ClassDeclaration classDecl = classType.getDeclaration();

        // On remonte la hiérarchie pour trouver l'attribut
        ClassDeclaration declaringClass = null;
        ClassDeclaration currentDecl = classDecl;
        while (currentDecl != null && this.attribute == null) {
            for (fr.n7.stl.minijava.ast.type.declaration.ClassElement element : currentDecl.getElements()) {
                if (element instanceof AttributeDeclaration) {
                    AttributeDeclaration attrDecl = (AttributeDeclaration) element;
                    if (attrDecl.getName().equals(this.name)) {
                        this.attribute = attrDecl;
                        declaringClass = currentDecl; // On retient où l'attribut est déclaré
                        break;
                    }
                }
            }
            if (this.attribute == null) {
                currentDecl = currentDecl.getAncestorDecl();
            }
        }

        if (this.attribute == null) {
            Logger.error("L'attribut " + this.name + " n'existe pas dans la classe " + classDecl.getName());
            return false;
        }

        // Vérification d'encapsulation
        fr.n7.stl.minijava.ast.type.declaration.AccessRight right = this.attribute.getAccessRight();
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
                // Private : accessible UNIQUEMENT dans la classe qui le déclare
                if (currentClassName == null || !currentClassName.equals(declaringClass.getName())) {
                    Logger.error("Encapsulation error: L'attribut " + this.name + " est privé dans la classe " + declaringClass.getName() + " et ne peut pas être accédé ici.");
                    return false;
                }
            } else {
                // Protected : accessible dans la classe déclarante et ses sous-classes
                boolean isSubclass = false;
                if (currentClassDecl instanceof fr.n7.stl.minic.ast.instruction.declaration.VariableDeclaration) {
                    Type t = ((fr.n7.stl.minic.ast.instruction.declaration.VariableDeclaration) currentClassDecl).getType();
                    if (t instanceof ClassType) {
                        ClassDeclaration curr = ((ClassType) t).getDeclaration();
                        while (curr != null) {
                            if (curr.getName().equals(declaringClass.getName())) {
                                isSubclass = true;
                                break;
                            }
                            curr = curr.getAncestorDecl();
                        }
                    }
                }
                if (!isSubclass) {
                    Logger.error("Encapsulation error: L'attribut " + this.name + " est protégé dans la classe " + declaringClass.getName() + " et ne peut pas être accédé ici.");
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public Type getType() {
        return this.attribute.getType();
    }

    @Override
    public String toString() {
        return this.object + "." + this.name;
    }

}
