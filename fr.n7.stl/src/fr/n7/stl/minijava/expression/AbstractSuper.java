package fr.n7.stl.minijava.expression;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.Expression;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;

public abstract class AbstractSuper<ObjectKind extends Expression> implements Expression {

	public AbstractSuper() {
		// TODO Auto-generated constructor stub
		throw new SemanticsUndefinedException("constructor in AbstractSuper");

	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		// TODO Auto-generated method stub
		throw new SemanticsUndefinedException("collectAndPartialResolve in AbstractSuper");

	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		// TODO Auto-generated method stub
		throw new SemanticsUndefinedException("completeResolve in AbstractSuper");

	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		throw new SemanticsUndefinedException("getType in AbstractSuper");

	}

	@Override
	public String toString() {
		return "super";
	}

}
