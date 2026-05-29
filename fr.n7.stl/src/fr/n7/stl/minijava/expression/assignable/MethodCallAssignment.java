package fr.n7.stl.minijava.expression.assignable;

import java.util.List;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.expression.assignable.AssignableExpression;
import fr.n7.stl.minijava.expression.AbstractMethodCall;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;

/**
 * Une affectation sur un appel de méthode n'a aucun sens en Java/MiniJava.
 * On ne peut pas faire : obj.getValeur() = 5;
 * 
 * Du coup, cette classe existe juste pour l'AST mais son getCode n'est 
 * jamais censé être appelé si le typage fait bien son boulot.
 */
public class MethodCallAssignment extends AbstractMethodCall<AssignableExpression> implements AssignableExpression {

	public MethodCallAssignment(AssignableExpression _target, String _name, List<AccessibleExpression> _arguments) {
		super(_target, _name, _arguments);
	}

	public MethodCallAssignment(String _name, List<AccessibleExpression> _arguments) {
		this(null, _name, _arguments);
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		throw new SemanticsUndefinedException("On ne peut pas affecter une valeur à un appel de méthode.");
	}

}
