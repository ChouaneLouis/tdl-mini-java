package fr.n7.stl.minijava.ast.type;

import javax.lang.model.type.NullType;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.AtomicType;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.util.Logger;

public class ClassType implements Type {

	protected String name;

	

	public ClassType(String _name) {
		this.name = _name;
	}

	@Override
	public boolean equalsTo(Type _other) {
		// TODO Auto-generated method stub
		if (_other instanceof ClassType) {
			return this.name.equals(((ClassType)_other).name);
		} else {
			return false;
		} 
	}

	@Override
	public boolean compatibleWith(Type _other) {
		// TODO Auto-generated method stub
		if ( this.equalsTo(_other) || _other == AtomicType.NullType) {
			return true;
		}
		return false;

	}

	@Override
	public Type merge(Type _other) {
		// TODO Auto-generated method stub
		if (this.compatibleWith(_other)) {
			return _other;
		} else if (_other.compatibleWith(this)) {
			return this;
		} else {
			Logger.error("Erreur de Type");
			return null;
		}

	}

	@Override
	public int length() {
		return 1;

	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		// TODO Auto-generated method stub
		return true;

	}

	public String toString() {
		return " " + this.name + " ";
	}

	public String getName() {
		return name;
	}

}
