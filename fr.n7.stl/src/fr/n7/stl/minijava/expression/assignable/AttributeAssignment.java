package fr.n7.stl.minijava.expression.assignable;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.accessible.FieldAccess;
import fr.n7.stl.minic.ast.expression.assignable.AssignableExpression;
import fr.n7.stl.minic.ast.expression.assignable.FieldAssignment;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.expression.AbstractAttribute;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;

public class AttributeAssignment extends AbstractAttribute<AssignableExpression> implements AssignableExpression {

	public AttributeAssignment(AssignableExpression _object, String _name) {
		super(_object, _name);
        this.field = new FieldAssignment(this.object, this.name);
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		return this.field.getCode(_factory);
	}

}
