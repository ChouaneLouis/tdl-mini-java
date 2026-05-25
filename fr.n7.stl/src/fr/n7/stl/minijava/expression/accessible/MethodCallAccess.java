package fr.n7.stl.minijava.expression.accessible;

import java.util.List;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.Expression;
import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.instruction.Instruction;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.declaration.MethodDeclaration;
import fr.n7.stl.minijava.expression.AbstractMethodCall;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;

public class MethodCallAccess extends AbstractMethodCall<AccessibleExpression> implements AccessibleExpression {

	public MethodCallAccess(AccessibleExpression _target, String _name, List<AccessibleExpression> _arguments) {
		super(_target, _name, _arguments);
	}

	public MethodCallAccess(String _name, List<AccessibleExpression> _arguments) {
		super(_name, _arguments);
	}



	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment result = _factory.createFragment();

		if (this.declaration != null && this.declaration.getElementKind() == fr.n7.stl.minijava.ast.type.declaration.ElementKind.CLASS) {
			// STATIC METHOD
			for (AccessibleExpression arg : this.arguments) {
				result.append(arg.getCode(_factory));
			}
		} else {
			// INSTANCE METHOD
			if (this.target != null) {
				result.append(this.target.getCode(_factory));
			} else {
				// C'est un appel de méthode sans cible explicite, donc ça s'applique sur 'this'.
				// On doit empiler l'adresse de l'objet courant qui est toujours à LB - 1.
				result.add(_factory.createLoad(Register.LB, -1, 1));
			}
			for (AccessibleExpression arg : this.arguments) {
				result.append(arg.getCode(_factory));
			}
		}

		String label = (this.declaration != null) ? this.declaration.getName() : this.name;
		result.add(_factory.createCall("Method_" + label, fr.n7.stl.tam.ast.Register.SB));

		return result;
	}

}
