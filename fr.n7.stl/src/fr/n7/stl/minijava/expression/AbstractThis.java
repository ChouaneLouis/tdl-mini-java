package fr.n7.stl.minijava.expression;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.Expression;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;

public abstract class AbstractThis<ObjectKind extends Expression> implements Expression {

	public AbstractThis() {
	}

	protected Declaration declaration;

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		return true;
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
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