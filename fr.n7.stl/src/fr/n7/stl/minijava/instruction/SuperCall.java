package fr.n7.stl.minijava.instruction;

import java.util.Iterator;
import java.util.List;

import fr.n7.stl.minic.ast.type.Type;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.instruction.Instruction;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minijava.ast.type.declaration.ConstructorDeclaration;
import fr.n7.stl.minijava.ast.type.declaration.MethodDeclaration;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;

public class SuperCall implements Instruction {

	protected ConstructorDeclaration constructor;

	protected List<AccessibleExpression> arguments;

	public SuperCall(List<AccessibleExpression> _arguments) {
		this.arguments = _arguments;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		boolean isValid = true;
		for (AccessibleExpression arg : this.arguments) {
			isValid = isValid && arg.collectAndPartialResolve(_scope);
		}
		return isValid;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
		return this.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		boolean isValid = true;
		for (AccessibleExpression arg : this.arguments) {
			isValid = isValid && arg.completeResolve(_scope);
		}
		
		Declaration superDecl = _scope.get("super");
		if (superDecl != null && superDecl.getType() instanceof fr.n7.stl.minijava.ast.type.ClassType) {
			fr.n7.stl.minijava.ast.type.ClassType superClassType = (fr.n7.stl.minijava.ast.type.ClassType) superDecl.getType();
			Declaration classDecl = _scope.get(superClassType.getName());
			if (classDecl instanceof fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration) {
				fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration parentClass = (fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration) classDecl;
				for (fr.n7.stl.minijava.ast.type.declaration.ClassElement element : parentClass.getElements()) {
					if (element instanceof ConstructorDeclaration) {
						if (element.getName().equals(parentClass.getName())) {
							this.constructor = (ConstructorDeclaration) element;
							return isValid;
						}
					}
				}
			}
		}
		fr.n7.stl.util.Logger.error("Constructeur parent introuvable pour super()");
		return false;
	}

	@Override
	public boolean checkType() {
		boolean isValid = true;
		if (this.constructor != null) {
			if (this.arguments.size() != this.constructor.getParameters().size()) {
				fr.n7.stl.util.Logger.error("L'appel à super() attend " + this.constructor.getParameters().size() + " argument(s), mais " + this.arguments.size() + " ont été fournis.");
				isValid = false;
			} else {
				for (int i = 0; i < this.arguments.size(); i++) {
					Type argType = this.arguments.get(i).getType();
					Type paramType = this.constructor.getParameters().get(i).getType();
					if (!argType.compatibleWith(paramType)) {
						fr.n7.stl.util.Logger.error("Type incorrect pour l'argument " + (i+1) + " dans super() : attendu " + paramType + ", reçu " + argType + ".");
						isValid = false;
					}
				}
			}
		}
		return isValid;
	}

	@Override
	public int allocateMemory(Register _register, int _offset) {
		return 0;
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment result = _factory.createFragment();

		// 1. On charge l'adresse de 'super' (qui est en fait l'adresse de 'this' actuel) au sommet de la pile
		result.add(_factory.createLoad(Register.LB, -1, 1));

		// 2. On empile les arguments passés au constructeur parent
		for (AccessibleExpression arg : this.arguments) {
			result.append(arg.getCode(_factory));
		}

		// 3. On effectue l'appel au constructeur parent
		String label = (this.constructor != null) ? this.constructor.getName() : "super";
		result.add(_factory.createCall("Constructor_" + label, Register.SB));

		// 4. Important : Dépiler l'adresse de l'objet laissée par le constructeur.
		// Contrairement à une allocation (new) où on garde l'adresse, un appel super() est une instruction qui ne doit pas laisser de déchets sur la pile !
		result.add(_factory.createPop(0, 1));

		return result;
	}

	@Override
	public String toString() {
		String image = "";
		image += "super( ";
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
