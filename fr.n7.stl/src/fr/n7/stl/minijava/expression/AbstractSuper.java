package fr.n7.stl.minijava.expression;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.Expression;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;

public abstract class AbstractSuper<ObjectKind extends Expression> implements Expression {

	protected Declaration thisDeclaration;
	protected fr.n7.stl.minijava.ast.type.ClassType superType;

	public AbstractSuper() {
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		return true;
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		if (_scope.knows("this")) {
			this.thisDeclaration = _scope.get("this");
			Type currentType = this.thisDeclaration.getType();
			if (currentType instanceof fr.n7.stl.minijava.ast.type.ClassType) {
				fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration currentClass = ((fr.n7.stl.minijava.ast.type.ClassType) currentType)
						.getDeclaration();
				if (currentClass != null && currentClass.getAncestor() != null) {
					this.superType = new fr.n7.stl.minijava.ast.type.ClassType(currentClass.getAncestor());
					return this.superType.completeResolve(_scope);
				} else {
					fr.n7.stl.util.Logger.error("Impossible d'utiliser 'super' car la classe n'a pas d'ancêtre.");
					return false;
				}
			}
		}
		fr.n7.stl.util.Logger.error("Impossible d'utiliser 'super' dans ce contexte.");
		return false;
	}

	@Override
	public Type getType() {
		return this.superType;
	}

	@Override
	public String toString() {
		return "super";
	}

}
