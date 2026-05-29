package fr.n7.stl.minijava.instruction;

import java.util.Iterator;
import java.util.List;

import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.instruction.Instruction;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minijava.ast.type.declaration.ConstructorDeclaration;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;

/**
 * Appel à un autre constructeur de la même classe : this(args);
 * 
 * Pareil que SuperCall, on le limite aux constructeurs via le marqueur.
 */
public class ThisCall implements Instruction {

	protected ConstructorDeclaration constructor;
	protected List<AccessibleExpression> arguments;

	public ThisCall(List<AccessibleExpression> _arguments) {
		this.arguments = _arguments;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		if (!_scope.knows("$isConstructor")) {
			fr.n7.stl.util.Logger.error("Impossible d'utiliser 'this()' hors d'un constructeur.");
			return false;
		}
		boolean ok = true;
		for (AccessibleExpression accessibleExpression : arguments) {
			ok = ok && accessibleExpression.collectAndPartialResolve(_scope);
		}
		return ok;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
		return this.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		boolean ok = true;
		for (AccessibleExpression accessibleExpression : arguments) {
			ok = ok && accessibleExpression.completeResolve(_scope);
		}
		
		// On cherche la classe à partir de 'this' pour trouver le constructeur ciblé
		Declaration thisDecl = _scope.get("this");
		if (thisDecl instanceof ParameterDeclaration) {
			Type thisType = ((ParameterDeclaration) thisDecl).getType();
			if (thisType instanceof fr.n7.stl.minijava.ast.type.ClassType) {
				fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration classDecl = ((fr.n7.stl.minijava.ast.type.ClassType) thisType).getDeclaration();
				for (fr.n7.stl.minijava.ast.type.declaration.ClassElement element : classDecl.getElements()) {
					if (element instanceof ConstructorDeclaration) {
						// On matche le constructeur par le nombre d'arguments (-1 car 'this' est compté dans parameters mais pas dans arguments)
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
		// Pas de vérif de type des arguments car on n'a pas l'overloading de toute façon
		return true;
	}

	@Override
	public int allocateMemory(Register _register, int _offset) {
		return _offset;
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment fragment = _factory.createFragment();

		// 1. Empiler les arguments
		int sizeArgs = 0;
		for (AccessibleExpression arg : this.arguments) {
			fragment.append(arg.getCode(_factory));
			sizeArgs += arg.getType().length();
		}

		// 2. Empiler 'this' en dernier (-1[LB] dans un constructeur)
		fragment.add(_factory.createLoad(Register.LB, -1, 1));

		// 3. Appel
		int totalParams = this.arguments.size() + 1;
		fragment.add(_factory.createCall("Constructor_" + this.constructor.getName() + "_" + totalParams, Register.SB));

		return fragment;
	}

	@Override
	public String toString() {
		String image = "this( ";
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
