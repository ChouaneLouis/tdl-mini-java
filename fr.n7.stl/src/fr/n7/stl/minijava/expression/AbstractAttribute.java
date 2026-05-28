package fr.n7.stl.minijava.expression;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.Expression;
import fr.n7.stl.minic.ast.instruction.declaration.TypeDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.minijava.ast.type.declaration.AttributeDeclaration;
import fr.n7.stl.minijava.ast.type.declaration.ClassElement;
import fr.n7.stl.util.Logger;

public abstract class AbstractAttribute<ObjectKind extends Expression> implements Expression {

	protected ObjectKind object;
	protected String name;
	protected AttributeDeclaration attribute;

	protected TypeDeclaration thisrecord;

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
		/// EDITED
		boolean ok = this.object.completeResolve(_scope);
		if (!ok)
			return false;

		Type objectType = this.object.getType();
		if (!(objectType instanceof ClassType)) {
			Logger.error("L'expression " + this.object + " n'est pas un objet. Impossible d'accéder à l'attribut "
					+ this.name);
			return false;
		}

		ClassType classType = (ClassType) objectType;
		for (ClassElement element : classType.getClassDeclaration().getElements()) {
			if (element instanceof AttributeDeclaration) {
				AttributeDeclaration a = (AttributeDeclaration) element;
				if (a.getName().equals(this.name)) {
					this.attribute = a;
					break;
				}
			}
		}

		if (this.attribute != null) {
			return true;
		} else {
			Logger.error("Attribut " + this.name + " introuvable dans la classe "
					+ classType.getClassDeclaration().getName());
			return false;
		}

	}

	@Override
	public Type getType() {
		// . EDITED
		return this.attribute.getType();
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
