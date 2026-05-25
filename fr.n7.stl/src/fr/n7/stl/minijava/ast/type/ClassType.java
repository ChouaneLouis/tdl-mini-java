package fr.n7.stl.minijava.ast.type;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration;

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
		// TODO Auto-generated method stub
		if (_other instanceof ClassType) {
			return this.name.equals(((ClassType) _other).name);
		}
		return false;

	}

	@Override
	public boolean compatibleWith(Type _other) {
		// TODO Auto-generated method stub
		// throw new SemanticsUndefinedException("compatibleWith in Type");
		// TODO héritage
		return this.equalsTo(_other);

	}

	@Override
	public Type merge(Type _other) {
		// TODO Auto-generated method stub
		throw new SemanticsUndefinedException("merge in Type");

	}

	@Override
	public int length() {
		// throw new SemanticsUndefinedException("length in Type");
		return 1; // C'est une adresse

	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		// TODO Auto-generated method stub
		// throw new SemanticsUndefinedException("completeResolve in Type");
		// 1. Si la classe est déjà résolue, on ne fait rien
		if (this.declaration != null) {
			return true;
		}

		// 2. On vérifie si le nom de la classe est connu dans le scope
		if (!_scope.knows(this.name)) {
			fr.n7.stl.util.Logger.error("Le type identificateur '" + this.name + "' n'est pas défini.");
			return false;
		}

		// 3. On récupère la déclaration associée
		Declaration decl = _scope.get(this.name);

		// 4. On s'assure que cette déclaration est bien une classe (et pas une variable
		// locale ou autre)
		if (decl instanceof ClassDeclaration) {
			this.declaration = (ClassDeclaration) decl;
			return true;
		} else {
			fr.n7.stl.util.Logger.error("L'identificateur '" + this.name + "' n'est pas une classe.");
			return false;
		}

	}

	public String toString() {
		return " " + this.name + " ";
	}

	public ClassDeclaration getDeclaration() {
		return this.declaration;
	}

}
