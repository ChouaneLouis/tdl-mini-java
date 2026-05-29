package fr.n7.stl.minijava.expression.assignable;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.assignable.AssignableExpression;
import fr.n7.stl.minijava.expression.AbstractSuper;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;

public class SuperAssignment extends AbstractSuper<AssignableExpression> implements AssignableExpression {

	public SuperAssignment() {
		super();
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment fragment = _factory.createFragment();
		if (this.thisDeclaration instanceof fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration) {
			int offset = ((fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration) this.thisDeclaration).getOffset();
			fragment.add(_factory.createLoadA(fr.n7.stl.tam.ast.Register.LB, offset));
		} else {
			fragment.add(_factory.createLoadA(fr.n7.stl.tam.ast.Register.LB, -1));
		}
		return fragment;
	}

}
