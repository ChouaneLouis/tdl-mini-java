package fr.n7.stl.minijava.ast.type.declaration;

import fr.n7.stl.minic.ast.type.Type;

/**
 * Déclaration d'un attribut de classe en MiniJava.
 * 
 * Un attribut, c'est juste un nom et un type, plus un offset qui sera calculé
 * plus tard par la classe lors de l'allocation mémoire.
 */
public class AttributeDeclaration extends ClassElement {

	protected Type type;
	// L'offset de l'attribut dans l'objet en mémoire (calculé dans ClassDeclaration)
	protected int offset;

	public AttributeDeclaration(String _name, Type _type) {
		super(_name);
		this.type = _type;
	}

	@Override
	public Type getType() {
		return this.type;
	}

	public int getOffset() {
		return this.offset;
	}

	@Override
	public String toString() {
		return this.accessRight + " " + type + " " + this.name + ";\n";
	}
}
