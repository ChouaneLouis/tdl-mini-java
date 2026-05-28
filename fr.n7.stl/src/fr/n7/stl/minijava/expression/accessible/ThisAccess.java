package fr.n7.stl.minijava.expression.accessible;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.expression.accessible.ParameterAccess;
import fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration;
import fr.n7.stl.minijava.expression.AbstractThis;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;

public class ThisAccess extends AbstractThis<AccessibleExpression> implements AccessibleExpression {

	public ThisAccess() {
		super();
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		ParameterAccess pa = new ParameterAccess((ParameterDeclaration) (this.thisObject));
		Fragment f = _factory.createFragment();
		f.append(pa.getCode(_factory));
		throw new SemanticsUndefinedException("GETCODE THIS ACCESS");
		// f.add(_factory.createLoadI(this.thisObject.));
		// return pa.getCode(_factory);
	}

}
