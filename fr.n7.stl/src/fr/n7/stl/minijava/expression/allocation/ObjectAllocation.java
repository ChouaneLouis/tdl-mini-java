package fr.n7.stl.minijava.expression.allocation;

import java.util.Iterator;
import java.util.List;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.expression.assignable.AssignableExpression;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.declaration.AttributeDeclaration;
import fr.n7.stl.minijava.ast.type.declaration.ClassElement;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Library;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;

public class ObjectAllocation implements AccessibleExpression, AssignableExpression {

	protected String name;

	protected List<AccessibleExpression> arguments;

	public ObjectAllocation(String _name, List<AccessibleExpression> _arguments) {
		this.name = _name;
		this.arguments = _arguments;
	}

	private fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration classDeclaration;
	private fr.n7.stl.minijava.ast.type.declaration.ConstructorDeclaration constructor;

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		boolean isValid = true;
		for (AccessibleExpression arg : this.arguments) {
			isValid = isValid && arg.collectAndPartialResolve(_scope);
		}
		return isValid;
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		boolean isValid = true;
		for (AccessibleExpression arg : this.arguments) {
			isValid = isValid && arg.completeResolve(_scope);
		}

		Declaration decl = _scope.get(this.name);
		if (decl instanceof fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration) {
			this.classDeclaration = (fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration) decl;

			// Chercher le constructeur
			for (fr.n7.stl.minijava.ast.type.declaration.ClassElement element : this.classDeclaration.getElements()) {
				if (element instanceof fr.n7.stl.minijava.ast.type.declaration.ConstructorDeclaration) {
					fr.n7.stl.minijava.ast.type.declaration.ConstructorDeclaration c = (fr.n7.stl.minijava.ast.type.declaration.ConstructorDeclaration) element;
					if (c.getName().equals(this.name)) {
						this.constructor = c;
						break;
					}
				}
			}
		} else {
			fr.n7.stl.util.Logger.error("Class " + this.name + " introuvable pour l'allocation.");
			isValid = false;
		}

		return isValid;
	}

	@Override
	public Type getType() {
		// Vérification des types des arguments ici car ObjectAllocation est une
		// Expression
		if (this.constructor != null) {
			if (this.arguments.size() != this.constructor.getParameters().size()) {
				fr.n7.stl.util.Logger
						.error("Le constructeur de " + this.name + " attend " + this.constructor.getParameters().size()
								+ " argument(s), mais " + this.arguments.size() + " ont été fournis.");
			} else {
				for (int i = 0; i < this.arguments.size(); i++) {
					Type argType = this.arguments.get(i).getType();
					Type paramType = this.constructor.getParameters().get(i).getType();
					if (!argType.compatibleWith(paramType)) {
						fr.n7.stl.util.Logger.error("Type incorrect pour l'argument " + (i + 1) + " du constructeur de "
								+ this.name + " : attendu " + paramType + ", reçu " + argType + ".");
					}
				}
			}
		}

		if (this.classDeclaration != null) {
			return new fr.n7.stl.minijava.ast.type.ClassType(this.classDeclaration);
		}
		return null;
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment result = _factory.createFragment();

		// 1. Compute object size
		int size = 0;
		if (this.classDeclaration != null) {
			for (ClassElement element : this.classDeclaration.getElements()) {
				if (element instanceof AttributeDeclaration) {
					size += ((AttributeDeclaration) element).getType().length();
				}
			}
		}

		// 2. Allocate heap memory — leaves address on top of stack
		result.add(_factory.createLoadL(size));
		result.add(Library.MAlloc);

		// 3. Duplicate the address so the constructor can consume its copy
		// while we keep the original as the result of the expression.
		result.add(_factory.createLoad(Register.ST, -1, 1)); // duplicate top-of-stack

		// 4. Push constructor arguments
		for (AccessibleExpression arg : this.arguments) {
			result.append(arg.getCode(_factory));
		}

		// 5. Call constructor — RETURN (0) N pops this+args, leaving original address
		if (this.constructor != null) {
			result.add(_factory.createCall(
					"Constructor_" + this.name, Register.SB));
		}

		return result;
	}

	@Override
	public String toString() {
		String image = "";
		image += "new " + this.name + "( ";
		Iterator<AccessibleExpression> iterator = this.arguments.iterator();
		if (iterator.hasNext()) {
			AccessibleExpression argument = iterator.next();
			image += argument;
			while (iterator.hasNext()) {
				argument = iterator.next();
				image += " ," + argument;
			}
		}
		image += ")";
		return image;
	}

}
