package fr.n7.stl.minijava.ast.type.declaration;

import fr.n7.stl.minic.ast.type.Type;

public class AttributeDeclaration extends ClassElement {
<<<<<<< HEAD
	
	protected Type type;
	protected int offset;

	

    public AttributeDeclaration( String _name, Type _type) {
=======

	protected Type type;
	protected int offset;

	public AttributeDeclaration(String _name, Type _type) {
>>>>>>> alexis_temp
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

	public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
