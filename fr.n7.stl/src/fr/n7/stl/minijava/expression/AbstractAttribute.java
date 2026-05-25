package fr.n7.stl.minijava.expression;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.Expression;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minic.ast.type.declaration.FieldDeclaration;
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.minijava.ast.type.declaration.AttributeDeclaration;
import fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration;
import fr.n7.stl.minijava.ast.type.declaration.ClassElement;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;

public abstract class AbstractAttribute<ObjectKind extends Expression> implements Expression {

	protected ObjectKind object;
	protected String name;
	protected AttributeDeclaration attribute;

	public AbstractAttribute(ObjectKind _object, String _name) {
		this.object = _object;
		this.name = _name;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		/// EDITED
		return object.collectAndPartialResolve(_scope);

	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		// 1. Résoudre d'abord l'objet
		boolean ok = this.object.completeResolve(_scope);
		if (!ok)
			return false;

		// 2. Récupérer le type de l'objet et vérifier que c'est bien une Classe
		Type objectType = this.object.getType();
		if (!(objectType instanceof ClassType)) {
			Logger.error("L'expression " + this.object + " n'est pas un objet. Impossible d'accéder à l'attribut "
					+ this.name);
			return false;
		}

		ClassType classType = (ClassType) objectType;
		ClassDeclaration classDecl = classType.getDeclaration();

		// 3. Chercher l'attribut dans les éléments de la classe
		for (ClassElement element : classDecl.getElements()) {

			if (element instanceof AttributeDeclaration) {
				AttributeDeclaration attrDecl = (AttributeDeclaration) element;
				if (attrDecl.getName().equals(this.name)) {
					// ÇA Y EST ! On a trouvé l'attribut, on le sauvegarde enfin !
					this.attribute = attrDecl;
					break;
				}
			}
		}

		if (this.attribute == null) {
			Logger.error("L'attribut " + this.name + " n'existe pas dans la classe " + classDecl.getName());
			return false;
		}

		return true;

	}

	@Override
	public Type getType() {
		// EDITED
		return object.getType();

	}

	@Override
	public String toString() {
		String image = "";
		image += this.object;
		image += ".";
		image += this.name;
		return image;
	}

}
