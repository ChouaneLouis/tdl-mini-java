package fr.n7.stl.minijava.expression;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.Expression;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;

public abstract class AbstractThis<ObjectKind extends Expression> implements Expression {

	public AbstractThis() {
		// TODO Auto-generated constructor stub
		throw new SemanticsUndefinedException("constructor in AbstractThis");

	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		// TODO Auto-generated method stub
		throw new SemanticsUndefinedException("collectAndPartialResolve in AbstractThis");

	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		// TODO Auto-generated method stub
		throw new SemanticsUndefinedException("completeResolve in AbstractThis");

	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		throw new SemanticsUndefinedException("getType in AbstractThis");

	}

	@Override
	public String toString() {
		return "this";
	}
}
