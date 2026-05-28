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
import fr.n7.stl.minijava.ast.type.declaration.ConstructorDeclaration;
import fr.n7.stl.minijava.ast.type.declaration.MethodDeclaration;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;

public class ThisCall implements Instruction {

	protected ConstructorDeclaration constructor;

	// Argument dans le sens this.argument
	protected List<AccessibleExpression> arguments;

	public ThisCall(List<AccessibleExpression> _arguments) {
		this.arguments = _arguments;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		/// EDITED
		boolean ok = true;
		for (AccessibleExpression accessibleExpression : arguments) {
			ok = ok && accessibleExpression.collectAndPartialResolve(_scope);
		}
		return ok;

	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
		/// EDITED
		return this.collectAndPartialResolve(_scope);

	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		boolean ok = true;
		for (AccessibleExpression accessibleExpression : arguments) {
			ok = ok && accessibleExpression.completeResolve(_scope);
		}
		
		// Find the class from 'this' parameter
		Declaration thisDecl = _scope.get("this");
		if (thisDecl instanceof ParameterDeclaration) {
			Type thisType = ((ParameterDeclaration) thisDecl).getType();
			if (thisType instanceof fr.n7.stl.minijava.ast.type.ClassType) {
				fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration classDecl = ((fr.n7.stl.minijava.ast.type.ClassType) thisType).getDeclaration();
				for (fr.n7.stl.minijava.ast.type.declaration.ClassElement element : classDecl.getElements()) {
					if (element instanceof ConstructorDeclaration) {
						if (((ConstructorDeclaration) element).getParameters().size() - 1 == this.arguments.size()) {
							this.constructor = (ConstructorDeclaration) element;
							break;
						}
					}
				}
			}
		}
		
		if (this.constructor == null) {
			fr.n7.stl.util.Logger.error("Impossible de trouver le constructeur pour l'appel à this().");
			return false;
		}
		
		return ok;
	}

	@Override
	public boolean checkType() {
		boolean ok = true;
		for (AccessibleExpression accessibleExpression : arguments) {
			// type checks could be added here if we had overloading support
		}
		return ok;
	}

	@Override
	public int allocateMemory(Register _register, int _offset) {
		return _offset; // It's an instruction, but doesn't allocate local variables
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment fragment = _factory.createFragment();

		int sizeArgs = 0;
		for (AccessibleExpression arg : this.arguments) {
			fragment.append(arg.getCode(_factory));
			sizeArgs += arg.getType().length();
		}

		// On empile 'this' pour l'appel au constructeur
		fragment.add(_factory.createLoad(Register.LB, -1, 1));

		// On appelle le constructeur
		int totalParams = this.arguments.size() + 1; // +1 pour 'this'
		fragment.add(_factory.createCall("Constructor_" + this.constructor.getName() + "_" + totalParams, Register.SB));

		return fragment;
	}

	@Override
	public String toString() {
		String image = "";
		image += "this( ";
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
