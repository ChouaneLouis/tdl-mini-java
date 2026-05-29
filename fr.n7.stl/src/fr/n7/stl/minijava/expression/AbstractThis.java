package fr.n7.stl.minijava.expression;

import fr.n7.stl.minic.ast.expression.Expression;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;

/**
 * Classe de base pour toutes les expressions qui représentent "this" dans MiniJava.
 *
 * "this" est un pointeur vers l'instance courante.
 * On le retrouve dans le scope sous le nom "this" (injecté comme paramètre par ClassDeclaration).
 * Si on est dans une méthode statique ou en dehors d'une méthode, "this" n'existe pas.
 */
public abstract class AbstractThis<ObjectKind extends Expression> implements Expression {

    public AbstractThis() {
    }

    protected Declaration declaration;

    @Override
    public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
        return true;
    }

    /**
     * On résout "this" en cherchant le paramètre "this" dans le scope.
     * S'il n'est pas là, on est dans un contexte où "this" n'est pas valide
     * (méthode statique, par exemple).
     */
    @Override
    public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
        System.out.println("test");
        this.declaration = _scope.get("this");
        if (this.declaration == null) {
            fr.n7.stl.util.Logger.error("L'utilisation de 'this' est interdite dans ce contexte.");
            return false;
        }
        return true;
    }

    @Override
    public Type getType() {
        if (this.declaration != null) {
            return this.declaration.getType();
        }
        return null;
    }

    @Override
    public String toString() {
        return "this";
    }
}