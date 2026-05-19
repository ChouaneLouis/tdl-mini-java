package fr.n7.stl.minijava.ast.type;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;

public class ClassType implements Type {

	protected String name;

	public ClassType(String _name) {
		this.name = _name;
	}

	@Override
	public boolean equalsTo(Type _other) {
		// TODO Auto-generated method stub
		throw new SemanticsUndefinedException("equalsTo in Type");
	}

	@Override
	public boolean compatibleWith(Type _other) {
		// TODO Auto-generated method stub
		throw new SemanticsUndefinedException("compatibleWith in Type");

	}

	@Override
	public Type merge(Type _other) {
		// TODO Auto-generated method stub
		throw new SemanticsUndefinedException("merge in Type");

	}

	@Override
	public int length() {
		throw new SemanticsUndefinedException("length in Type");

	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		// TODO Auto-generated method stub
		throw new SemanticsUndefinedException("completeResolve in Type");

	}

	public String toString() {
		return " " + this.name + " ";
	}

}
