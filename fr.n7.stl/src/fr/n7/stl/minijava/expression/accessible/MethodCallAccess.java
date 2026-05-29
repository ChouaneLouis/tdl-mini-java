package fr.n7.stl.minijava.expression.accessible;

import java.util.List;

import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minijava.expression.AbstractMethodCall;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;

/**
 * Appel de méthode utilisé comme une expression (rvalue).
 * Ex: int x = a.getValeur();
 * 
 * Tout le boulot d'analyse sémantique est fait dans AbstractMethodCall.
 * La génération de code est déléguée au FunctionCall sous-jacent de miniC
 * qui sait comment empiler les paramètres et appeler la fonction.
 */
public class MethodCallAccess extends AbstractMethodCall<AccessibleExpression> implements AccessibleExpression {

	public MethodCallAccess(AccessibleExpression _target, String _name, List<AccessibleExpression> _arguments) {
		super(_target, _name, _arguments);
	}

	public MethodCallAccess(String _name, List<AccessibleExpression> _arguments) {
		super(_name, _arguments);
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		// Le FunctionCall va appeler la méthode.
		// Une fois la méthode terminée (via son RETURN), la valeur de retour
		// se retrouvera au sommet de la pile, prête à être utilisée.
		return this.call.getCode(_factory);
	}

}
