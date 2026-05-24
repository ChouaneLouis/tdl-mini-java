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

		// 1. On charge l'adresse de l'objet (la cible) au sommet de la pile
		// Cela servira de paramètre implicite 'this' pour la méthode
		if (this.target != null) {
			result.append(this.target.getCode(_factory));
		}

		// 2. On évalue et on empile chaque argument passé à la méthode
		for (AccessibleExpression arg : this.arguments) {
			result.append(arg.getCode(_factory));
		}

		// 3. On effectue l'appel à la méthode
		// On utilise le nom de la méthode (ou celui de sa déclaration)
		String label = (this.declaration != null) ? this.declaration.getName() : this.name;
		result.add(_factory.createCall(label, Register.SB));

		return result;
	}

}
