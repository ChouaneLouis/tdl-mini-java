package fr.n7.stl.minijava.expression.accessible;

import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minijava.expression.AbstractSuper;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;

/**
 * Accès à "super" en tant qu'expression (rvalue).
 * Ex : return super; ou super.foo();
 * 
 * Tout comme "this", "super" est juste un pointeur vers l'objet courant,
 * c'est juste son type (ClassType parent) qui change à la compilation.
 * À l'exécution, c'est exactement la même adresse mémoire que "this".
 */
public class SuperAccess extends AbstractSuper<AccessibleExpression> implements AccessibleExpression {

	public SuperAccess() {
		super();
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment fragment = _factory.createFragment();
		// On charge l'adresse de l'objet (le pointeur "this")
		if (this.thisDeclaration instanceof fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration) {
			int offset = ((fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration) this.thisDeclaration).getOffset();
			fragment.add(_factory.createLoad(fr.n7.stl.tam.ast.Register.LB, offset, 1));
		} else {
			fragment.add(_factory.createLoad(fr.n7.stl.tam.ast.Register.LB, -1, 1));
		}
		return fragment;
	}

}
