package fr.n7.stl.minijava.ast.type.declaration;

import java.util.Iterator;
import java.util.List;

import fr.n7.stl.minic.ast.Block;
import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.scope.SymbolTable;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;

public class ConstructorDeclaration extends ClassElement {

	protected List<ParameterDeclaration> parameters;

	protected Block body;

	public ConstructorDeclaration(String _name, List<ParameterDeclaration> _parameters, Block _body) {
		super(_name);
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
		// TODO Auto-generated method stub
		throw new SemanticsUndefinedException("Semantics get type is undefined in ConstructorDeclaration.");

		// return null;
	}

	protected HierarchicalScope<Declaration> consScope;

	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		this.consScope = new SymbolTable(_scope);
		for (ParameterDeclaration parameterDeclaration : parameters) {
			consScope.register(parameterDeclaration);
		}
		return this.body.collectAndPartialResolve(consScope);
	}

	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		return this.body.completeResolve(consScope);
	}

	public Fragment getCode(TAMFactory _factory) {
		Fragment cons = body.getCode(_factory);
		cons.addPrefix("Constructor_" + this.name);

		int sizeOfParams = 0;
		for (ParameterDeclaration parameterDeclaration : parameters) {
			sizeOfParams += parameterDeclaration.getType().length();
		}
		cons.add(_factory.createReturn(0, sizeOfParams));
		return cons;
	}
}
