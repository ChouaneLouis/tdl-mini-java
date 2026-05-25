package fr.n7.stl.minijava.expression;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.Expression;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minic.ast.type.declaration.FieldDeclaration;
import fr.n7.stl.minijava.ast.type.declaration.AttributeDeclaration;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;

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
		return this.object.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		boolean ok = this.object.completeResolve(_scope);
		if (!ok){return false;}

		Type objectType = this.object.getType();

		if (objectType instanceof fr.n7.stl.minijava.ast.type.ClassType){
			// On utilise getDeclaration() pour éviter de retomber sur le constructeur si on fait juste _scope.get()
			fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration laClass = ((fr.n7.stl.minijava.ast.type.ClassType) objectType).getDeclaration();
			
			if (laClass != null){
				// Boucle ajoutée pour remonter dans les classes parentes si on ne trouve pas l'attribut dans la classe courante
				while (laClass != null) {
					for (fr.n7.stl.minijava.ast.type.declaration.ClassElement element : laClass.getElements()) {
						if (element instanceof fr.n7.stl.minijava.ast.type.declaration.AttributeDeclaration){
							fr.n7.stl.minijava.ast.type.declaration.AttributeDeclaration attr = (fr.n7.stl.minijava.ast.type.declaration.AttributeDeclaration) element;
							if (attr.getName().equals(this.name)){
								this.attribute = attr;
								return true;
							}
						}
					}
					if (laClass.getAncestor() != null) {
						Declaration ancestorDecl = _scope.get(laClass.getAncestor());
						if (ancestorDecl instanceof fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration) {
							laClass = (fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration) ancestorDecl;
						} else {
							laClass = null;
						}
					} else {
						laClass = null;
					}
				}
			}
		}
		fr.n7.stl.util.Logger.error("L'attribut " + this.name + " n'existe pas ou l'objet cible est invalide.");
		return false;
	}

	@Override
	public Type getType() {
		if (this.attribute != null) {
            return this.attribute.getType();
        }
        return null;
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
