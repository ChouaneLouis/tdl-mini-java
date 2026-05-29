package fr.n7.stl.minijava.instruction;

import java.util.Iterator;
import java.util.List;

import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.instruction.Instruction;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minijava.ast.type.declaration.ConstructorDeclaration;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;

/**
 * Appel au constructeur de la classe mère : super(args);
 * 
 * Ce call ne peut être fait qu'à l'intérieur d'un constructeur.
 * On utilise le marqueur $isConstructor pour vérifier ça.
 */
public class SuperCall implements Instruction {

	protected ConstructorDeclaration constructor;
	protected List<AccessibleExpression> arguments;
	protected String ancestorName;
	protected Declaration thisDeclaration;

	public SuperCall(List<AccessibleExpression> _arguments) {
		this.arguments = _arguments;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		// Le fameux marqueur injecté par ConstructorDeclaration
		if (!_scope.knows("$isConstructor")) {
			fr.n7.stl.util.Logger.error("Impossible d'utiliser 'super()' hors d'un constructeur.");
			return false;
		}
		boolean ok = true;
		for (AccessibleExpression arg : this.arguments) {
			ok = ok && arg.collectAndPartialResolve(_scope);
		}
		return ok;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
		return collectAndPartialResolve(_scope);
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		boolean ok = true;
		for (AccessibleExpression arg : this.arguments) {
			ok = ok && arg.completeResolve(_scope);
		}
		
		// On récupère "this" pour connaître sa classe et trouver son ancêtre
		if (_scope.knows("this")) {
			this.thisDeclaration = _scope.get("this");
			fr.n7.stl.minic.ast.type.Type currentType = this.thisDeclaration.getType();
			if (currentType instanceof fr.n7.stl.minijava.ast.type.ClassType) {
				fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration currentClass = ((fr.n7.stl.minijava.ast.type.ClassType) currentType).getDeclaration();
				if (currentClass != null && currentClass.getAncestor() != null) {
					this.ancestorName = currentClass.getAncestor();
				} else {
					fr.n7.stl.util.Logger.error("Impossible d'utiliser 'super()' car la classe n'a pas d'ancêtre.");
					return false;
				}
			}
		} else {
			// Double sécurité
			fr.n7.stl.util.Logger.error("Impossible d'utiliser 'super()' hors d'un constructeur.");
			return false;
		}

		return ok;
	}

	@Override
	public boolean checkType() {
		return true;
	}

	@Override
	public int allocateMemory(Register _register, int _offset) {
		return _offset;
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment fragment = _factory.createFragment();
		
		// 1. On empile les arguments
		for (AccessibleExpression arg : this.arguments) {
			fragment.append(arg.getCode(_factory));
		}
		
		// 2. On empile 'this' en dernier (offset récupéré ou -1 par défaut pour un constructeur)
		if (this.thisDeclaration instanceof fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration) {
			int offset = ((fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration) this.thisDeclaration).getOffset();
			fragment.add(_factory.createLoad(fr.n7.stl.tam.ast.Register.LB, offset, 1));
		} else {
			fragment.add(_factory.createLoad(fr.n7.stl.tam.ast.Register.LB, -1, 1));
		}
		
		// 3. Appel du constructeur parent
		int totalParams = this.arguments.size() + 1; // +1 pour 'this'
		fragment.add(_factory.createCall("Constructor_" + this.ancestorName + "_" + totalParams, fr.n7.stl.tam.ast.Register.SB));
		
		return fragment;
	}

	@Override
	public String toString() {
		String image = "super( ";
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
