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

	protected fr.n7.stl.minic.ast.expression.FunctionCall call;
	protected List<AccessibleExpression> arguments;

	public MethodCall(AccessibleExpression _target, String _name, List<AccessibleExpression> _arguments) {
		this.name = _name;
		this.method = null;
		this.target = _target;
		if (this.target == null) {
			this.target = new fr.n7.stl.minijava.expression.accessible.ThisAccess();
		}
		this.arguments = _arguments;
		
		java.util.List<AccessibleExpression> allArgs = new java.util.LinkedList<>();
		allArgs.add(this.target);
		allArgs.addAll(this.arguments);
		this.call = new fr.n7.stl.minic.ast.expression.FunctionCall(name, allArgs);
	}

	public MethodCall(String _name, List<AccessibleExpression> _arguments) {
		this(null, _name, _arguments);
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		return this.call.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
		return this.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		Declaration d = _scope.get(name);
		if (d instanceof MethodDeclaration) {
			this.method = (MethodDeclaration) d;
			if (this.method.getElementKind() == fr.n7.stl.minijava.ast.type.declaration.ElementKind.CLASS) {
				this.call = new fr.n7.stl.minic.ast.expression.FunctionCall(name, this.arguments);
				this.call.collectAndPartialResolve(_scope);
			}
		}
		boolean ok = this.call.completeResolve(_scope);
		return ok;
	}

	@Override
	public boolean checkType() {
		return true; // FunctionCall doesn't implement checkType, so we just assume it's true for now, or we can check arguments later if needed.
	}

	@Override
	public int allocateMemory(Register _register, int _offset) {
		return _offset;
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment f = this.call.getCode(_factory);
		if (this.call.getType().length() > 0) {
			f.add(_factory.createPop(0, this.call.getType().length()));
		}
		return f;
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
