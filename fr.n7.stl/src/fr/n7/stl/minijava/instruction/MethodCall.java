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
		boolean isValid = true;
		if (this.target != null) {
			isValid = isValid && this.target.collectAndPartialResolve(_scope);
		}
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
		if (this.target != null) {
			isValid = isValid && this.target.completeResolve(_scope);
		}
		for (AccessibleExpression arg : this.arguments) {
			isValid = isValid && arg.completeResolve(_scope);
		}

		fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration classDecl = null;
		if (this.target != null) {
			fr.n7.stl.minic.ast.type.Type targetType = this.target.getType();
			if (targetType instanceof fr.n7.stl.minijava.ast.type.ClassType) {
				// Pareil ici, getDeclaration() évite de foirer la recherche avec _scope.get()
				classDecl = ((fr.n7.stl.minijava.ast.type.ClassType) targetType).getDeclaration();
			}
		} else {
			Declaration thisDecl = _scope.get("this");
			if (thisDecl != null && thisDecl.getType() instanceof fr.n7.stl.minijava.ast.type.ClassType) {
				classDecl = ((fr.n7.stl.minijava.ast.type.ClassType) thisDecl.getType()).getDeclaration();
			}
		}

		if (classDecl != null) {
			// Pareil, on remonte les ancêtres pour trouver la méthode (héritage)
			while (classDecl != null) {
				for (fr.n7.stl.minijava.ast.type.declaration.ClassElement element : classDecl.getElements()) {
					if (element instanceof MethodDeclaration && element.getName().equals(this.name)) {
						this.method = (MethodDeclaration) element;
						return isValid;
					}
				}
				if (classDecl.getAncestor() != null) {
					Declaration ancestorDecl = _scope.get(classDecl.getAncestor());
					if (ancestorDecl instanceof fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration) {
						classDecl = (fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration) ancestorDecl;
					} else {
						classDecl = null;
					}
				} else {
					classDecl = null;
				}
			}
		}

		fr.n7.stl.util.Logger.error("Method " + this.name + " not found or invalid target in instruction.");
		return false;
	}

	@Override
	public boolean checkType() {
		boolean isValid = true;
		if (this.method == null)
			return false;

		List<ParameterDeclaration> params = this.method.getParameters();
		if (params.size() != this.arguments.size()) {
			fr.n7.stl.util.Logger.error("Wrong number of arguments for method " + this.name);
			return false;
		}

		// On vérifie bien que chaque type d'argument correspond au type du paramètre
		// déclaré
		for (int i = 0; i < params.size(); i++) {
			fr.n7.stl.minic.ast.type.Type argType = this.arguments.get(i).getType();
			fr.n7.stl.minic.ast.type.Type paramType = params.get(i).getType();
			if (!argType.compatibleWith(paramType)) {
				fr.n7.stl.util.Logger.error("Incompatible argument type for method " + this.name + ": expected "
						+ paramType + " but got " + argType);
				isValid = false;
			}
		}
		return isValid;
	}

	@Override
	public int allocateMemory(Register _register, int _offset) {
		return _offset;
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment result = _factory.createFragment();

		if (this.method != null
				&& this.method.getElementKind() == fr.n7.stl.minijava.ast.type.declaration.ElementKind.CLASS) {
			// STATIC METHOD
			for (AccessibleExpression arg : this.arguments) {
				result.append(arg.getCode(_factory));
			}
		} else {
			// INSTANCE METHOD
			if (this.target != null) {
				result.append(this.target.getCode(_factory));
			} else {
				// implicit 'this'
				result.add(_factory.createLoad(Register.LB, -1, 1));
			}
			for (AccessibleExpression arg : this.arguments) {
				result.append(arg.getCode(_factory));
			}
		}

		// 3. On effectue l'appel à la méthode
		String label = (this.method != null) ? this.method.getName() : this.name;
		result.add(_factory.createCall("Method_" + label, Register.SB));

		// 4. Si la méthode renvoie une valeur, on doit la dépiler car c'est une
		// instruction (on ignore le résultat)
		if (this.method != null && this.method.getType() != fr.n7.stl.minic.ast.type.AtomicType.VoidType) {
			result.add(_factory.createPop(0, this.method.getType().length()));
		}

		return result;
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
