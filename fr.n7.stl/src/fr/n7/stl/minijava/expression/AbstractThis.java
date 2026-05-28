package fr.n7.stl.minijava.expression;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.Expression;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.expression.allocation.ObjectAllocation;
import fr.n7.stl.util.Logger;

public abstract class AbstractThis<ObjectKind extends Expression> implements Expression {

	protected Declaration thisObject;

	public AbstractThis() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		/// EDITED
		return true;
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		/// EDITED
		thisObject = _scope.get("this");
		if (thisObject != null) {
			return true;
		} else {
			Logger.error("thisObject null");
		}
		return false;
	}

	@Override
	public Type getType() {
		/// EDITED
		return thisObject.getType();
	}

	@Override
	public String toString() {
		return "this";
	}
}
