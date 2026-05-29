package fr.n7.stl.minijava.ast.type;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration;

/**
 * Représente un type objet (une classe) dans MiniJava.
 * 
 * Sa taille est toujours de 1 mot (c'est juste une adresse/un pointeur vers le tas).
 * Il gère aussi la compatibilité de type via l'héritage (polymorphisme).
 */
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
		if (_other instanceof ClassType) {
			return this.name.equals(((ClassType) _other).name);
		}
		return false;
	}

	/**
	 * Vérifie si ce type est compatible avec _other.
	 * 
	 * Si les types sont identiques, c'est bon.
	 * Sinon, on remonte la hiérarchie d'héritage pour voir si ce type
	 * hérite de _other.
	 */
	@Override
	public boolean compatibleWith(Type _other) {
		if (this.equalsTo(_other)) return true;
		
		if (_other instanceof ClassType && this.declaration != null) {
			String parentName = this.declaration.getAncestor();
			if (parentName != null) {
				// On remonte les ancêtres pour voir si l'un d'eux correspond à _other
				ClassDeclaration current = this.declaration;
				while (current != null) {
					if (current.getName().equals(((ClassType)_other).name)) {
						return true;
					}
					current = current.getAncestorDecl();
				}
			}
		}
		return false;
	}

	@Override
	public Type merge(Type _other) {
		throw new SemanticsUndefinedException("merge in Type");
	}

	/**
	 * En MiniJava, les objets sont manipulés par référence.
	 * La taille d'une variable de type classe est donc la taille d'un pointeur (1 mot).
	 */
	@Override
	public int length() {
		return 1;
	}

	/**
	 * Résout la classe correspondante dans le scope.
	 */
	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		// Déjà résolu
		if (this.declaration != null) {
			return true;
		}

		if (!_scope.knows(this.name)) {
			fr.n7.stl.util.Logger.error("Le type identificateur '" + this.name + "' n'est pas défini.");
			return false;
		}

		Declaration decl = _scope.get(this.name);

		// On vérifie que c'est bien une classe et pas autre chose (ex: une variable)
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
