package fr.n7.stl.minijava.ast.type;

import javax.lang.model.type.NullType;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.AtomicType;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration;
<<<<<<< HEAD
import fr.n7.stl.util.Logger;
=======
>>>>>>> alexis_temp

public class ClassType implements Type {

	protected String name;
	protected ClassDeclaration declaration;

	public ClassType(String _name) {
		this.name = _name;
		this.declaration = null;
	}

	public ClassType(ClassDeclaration _declaration) {
<<<<<<< HEAD
        this.name = _declaration.getName();
        this.declaration = _declaration;
    }

	@Override
	public boolean equalsTo(Type _other) {
		if (_other instanceof ClassType) {
			return this.name.equals(((ClassType)_other).name);
		} else {
			return false;
		} 
=======
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

>>>>>>> alexis_temp
	}

	@Override
	public boolean compatibleWith(Type _other) {
<<<<<<< HEAD
		if (this.equalsTo(_other) || _other == AtomicType.NullType) {
			return true;
		}
		if (_other instanceof ClassType) {
			ClassDeclaration otherDecl = ((ClassType)_other).getDeclaration();
			// On remonte les ancêtres de 'this' pour voir si on hérite de '_other'
			ClassDeclaration current = this.declaration;
			while (current != null && current.getAncestor() != null) {
				if (current.getAncestor().equals(((ClassType)_other).getName())) {
					return true;
				}
				// On n'a pas accès direct au scope ici pour résoudre l'ancêtre récursivement proprement,
				// mais dans un langage simple on compare le nom de l'ancêtre.
				// (L'idéal serait de garder la référence de la classe parente dans ClassDeclaration)
				// Si besoin on peut se limiter à 1 niveau d'héritage pour ce test.
				current = null; // Simplification car on ne peut pas résoudre facilement l'ancêtre sans scope
			}
		}
		return false;
=======
		// TODO Auto-generated method stub
		// throw new SemanticsUndefinedException("compatibleWith in Type");
		// TODO héritage
		return this.equalsTo(_other);

>>>>>>> alexis_temp
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
<<<<<<< HEAD
		return 1;
=======
		// throw new SemanticsUndefinedException("length in Type");
		return 1; // C'est une adresse
>>>>>>> alexis_temp

	}

	@Override
<<<<<<< HEAD
    public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
        // C'est ici qu'on relie le nom de la classe lu par le parser à sa vraie déclaration !
        Declaration decl = _scope.get(this.name);
        if (decl != null && decl instanceof ClassDeclaration) {
            this.declaration = (ClassDeclaration) decl;
            return true;
        } else {
            Logger.error("La classe " + this.name + " n'a pas été déclarée ou est introuvable.");
            return false;
        }
    }
=======
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
>>>>>>> alexis_temp

	public String toString() {
		return " " + this.name + " ";
	}

<<<<<<< HEAD
	public String getName() {
		return name;
	}

	public ClassDeclaration getDeclaration() {
		// On ajoute ça pour récupérer la déclaration de la classe directement au lieu de chercher par le nom, ça évite les conflits avec le nom des constructeurs !
		return declaration;
=======
	public ClassDeclaration getDeclaration() {
		return this.declaration;
>>>>>>> alexis_temp
	}

}
