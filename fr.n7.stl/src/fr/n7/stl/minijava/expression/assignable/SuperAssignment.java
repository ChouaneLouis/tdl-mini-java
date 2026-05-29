package fr.n7.stl.minijava.expression.assignable;

import fr.n7.stl.minic.ast.expression.assignable.AssignableExpression;
import fr.n7.stl.minijava.expression.AbstractSuper;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;

/**
 * Représente "super" utilisé comme cible d'affectation (lvalue).
 * C'est très rare/inutile (on ne fait pas "super = ..."), mais ça
 * peut servir si on l'utilise pour accéder à un attribut (super.v = 5).
 * Dans ce cas, super renvoie sa propre adresse pour que AttributeAssignment
 * sache où écrire.
 */
public class SuperAssignment extends AbstractSuper<AssignableExpression> implements AssignableExpression {

	public SuperAssignment() {
		super();
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment fragment = _factory.createFragment();
		
		// On charge l'ADRESSE de la variable 'this' (qui contient le pointeur vers l'objet)
		if (this.thisDeclaration instanceof fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration) {
			int offset = ((fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration) this.thisDeclaration).getOffset();
			fragment.add(_factory.createLoadA(fr.n7.stl.tam.ast.Register.LB, offset));
		} else {
			// Fallback (ex: constructeur avec this à -1)
			fragment.add(_factory.createLoadA(fr.n7.stl.tam.ast.Register.LB, -1));
		}
		
		return fragment;
	}

}
