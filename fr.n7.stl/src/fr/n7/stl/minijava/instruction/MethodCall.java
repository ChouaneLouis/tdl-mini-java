package fr.n7.stl.minijava.instruction;

import java.util.Iterator;
import java.util.List;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.instruction.Instruction;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minijava.ast.type.declaration.MethodDeclaration;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;

//Se suffit dans l'appel ex : a.setV(9), ou on ignore la valeur;
// target.method(arguments)

public class MethodCall implements Instruction {

	protected AccessibleExpression target;

	protected String name;

	protected MethodDeclaration method;

	protected List<AccessibleExpression> arguments;

	public MethodCall(AccessibleExpression _target, String _name, List<AccessibleExpression> _arguments) {
		this.name = _name;
		this.method = null;
		this.target = _target;
		this.arguments = _arguments;
	}

	public MethodCall(String _name, List<AccessibleExpression> _arguments) {
		this(null, _name, _arguments);
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		/// EDITED
		return this.target.collectAndPartialResolve(_scope);

	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
		/// EDITED
		return this.collectAndPartialResolve(_scope);

	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		/// EDITED
		if (!_scope.knows(name)) {
			Logger.error(name + " n'est pas connu");
			return false;
		}
		Declaration d = _scope.get(name);
		if (d instanceof MethodDeclaration) {
			this.method = (MethodDeclaration) d;

			for (AccessibleExpression accessibleExpression : arguments) {
				System.out.println(accessibleExpression.getClass().toString());
			}

			return this.target.completeResolve(_scope);
		}
		Logger.error(name + "n'est pas une methode");
		return false;

	}

	@Override
	public boolean checkType() {
		// TODO Auto-generated method stub
		throw new SemanticsUndefinedException("checkType in MethodCall");

	}

	@Override
	public int allocateMemory(Register _register, int _offset) {
		// TODO Auto-generated method stub
		throw new SemanticsUndefinedException("allocateMemory in MethodCall");

	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		// TODO Auto-generated method stub
		throw new SemanticsUndefinedException("getCode in MethodCall");

	}

	@Override
	public String toString() {
		String image = "";
		if (this.target != null) {
			image += target + ".";
		}
		image += this.name;
		image += "( ";
		Iterator<AccessibleExpression> iterator = this.arguments.iterator();
		if (iterator.hasNext()) {
			AccessibleExpression argument = iterator.next();
			image += argument;
			while (iterator.hasNext()) {
				argument = iterator.next();
				image += " ," + argument;
			}
		}
		image += ");\n";
		return image;
	}

}
