package fr.n7.stl.minijava.expression.accessible;

import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minijava.expression.AbstractThis;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;

/**
 * Accès à l'instance courante "this" en rvalue.
 * Ex : return this; ou this.foo();
 * 
 * En mémoire, "this" est juste un paramètre invisible empilé au début
 * de l'appel de la méthode (ou à la fin pour les constructeurs).
 */
public class ThisAccess extends AbstractThis<AccessibleExpression> implements AccessibleExpression {

	public ThisAccess() {
		super();
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment f = _factory.createFragment();
		
		// On récupère "this" dans le scope (c'est un ParameterDeclaration)
		if (this.declaration instanceof fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration) {
			int offset = ((fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration) this.declaration).getOffset();
			// On charge la valeur du pointeur this depuis la pile
			f.add(_factory.createLoad(Register.LB, offset, 1));
		} else {
			// Fallback au cas où (par défaut, this est souvent à -1[LB] pour les constructeurs)
			f.add(_factory.createLoad(Register.LB, -1, 1));
		}
		
		return f;
	}

}
