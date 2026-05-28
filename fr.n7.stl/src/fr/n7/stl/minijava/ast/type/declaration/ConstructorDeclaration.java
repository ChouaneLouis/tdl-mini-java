package fr.n7.stl.minijava.ast.type.declaration;

import java.util.Iterator;
import java.util.List;

import fr.n7.stl.minic.ast.Block;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minic.ast.type.AtomicType;

public class ConstructorDeclaration extends ClassElement {
	
	protected List<ParameterDeclaration> parameters;
	
	protected Block body;

    protected ClassDeclaration owner;
    protected FunctionDeclaration function;

	public ConstructorDeclaration(String _name, List<ParameterDeclaration> _parameters, Block _body) {
		super( _name);
		this.parameters = _parameters;
		this.body = _body;
	}

	@Override
	public String toString() {
		String image = "";
		image += this.accessRight + " " + this.name + "( ";
		Iterator<ParameterDeclaration> iterator = this.parameters.iterator();
		if (iterator.hasNext()) {
			ParameterDeclaration parameter = iterator.next();
			image += parameter;
			while (iterator.hasNext()) {
				 parameter = iterator.next();
				 image += " ," + parameter;
			}
		}
		image += ")";
		image += this.body; 
		return image;
	}

	@Override
	public Type getType() {
		return this.owner.getType();
	}

    public void declareFunction(ClassDeclaration _owner, int id) {
        this.owner = _owner;
        this.name = "_new_" + this.name + "_" + id;
        this.parameters.addFirst(new ParameterDeclaration("this", this.getType()));
        this.function = new FunctionDeclaration(
                this.name,
                AtomicType.VoidType,
                this.parameters,
                this.body
            );
    }

    public FunctionDeclaration getFunction() {
        return this.function;
    }
}
