package fr.n7.stl.minijava.ast.type;

import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minic.ast.type.RecordType;
import fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration;
import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.util.Logger;

public class ClassType implements Type {
	
	protected String name;
    protected ClassDeclaration declaration;

	public ClassType(String _name) {
		this.name = _name;
	}

    public ClassType(ClassDeclaration _declaration) {
        this.declaration = _declaration;
        this.name = _declaration.getName();
    }

	@Override
	public boolean equalsTo(Type _other) {
        /// EDITED
        return _other instanceof ClassType && this.declaration.equals(((ClassType) _other).getClassDeclaration());
	}

	@Override
	public boolean compatibleWith(Type _other) {
        /// EDITED
        return this.equalsTo(_other); // TODO a modifier pour accepter l'héritage
	}

	@Override
	public Type merge(Type _other) {
        throw new SemanticsUndefinedException("merge in ClassType");
	}

	@Override
	public int length() {
        /// EDITED
		return 1;
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
        /// EDITED
        if (this.declaration != null) return true;

        if (_scope.knows(this.name) && _scope.get(this.name) instanceof ClassDeclaration) {
            this.declaration = (ClassDeclaration) _scope.get(this.name);
            return true;
        }
        Logger.error(_scope.knows(this.name) ?
                "L'identificateur " + this.name + " n'est pas une class" :
                "Le type " + this.name + " n'est pas définie");
		return false;
	}
	
	public String toString() {
		return " " + this.name + " ";
	}

    public ClassDeclaration getClassDeclaration() {
        return this.declaration;
    }

}
