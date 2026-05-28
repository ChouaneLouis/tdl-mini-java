package fr.n7.stl.minijava.expression.accessible;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.expression.accessible.BinaryOperator;
import fr.n7.stl.minic.ast.instruction.declaration.VariableDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.declaration.AttributeDeclaration;
import fr.n7.stl.minijava.expression.AbstractAttribute;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.tam.ast.TAMInstruction;
import fr.n7.stl.util.Logger;

public class AttributeAccess extends AbstractAttribute<AccessibleExpression> implements AccessibleExpression {

	public AttributeAccess(AccessibleExpression _object, String _name) {
		super(_object, _name);
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		return this.object.collectAndPartialResolve(_scope);
	}





	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment f = _factory.createFragment();
		// code du this : on suppose l'adresse en haut de la pile
		f.append(this.object.getCode(_factory));
		f.add(_factory.createLoadL(this.attribute.getOffset()));
		f.add(TAMFactory.createBinaryOperator(BinaryOperator.Add));

		f.add(_factory.createLoadI(this.attribute.getType().length()));

		return f;

	}

}
