package fr.n7.stl.minijava.expression.accessible;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.expression.accessible.IdentifierAccess;
import fr.n7.stl.minic.ast.expression.accessible.FieldAccess;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.expression.AbstractAttribute;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;

public class AttributeAccess extends AbstractAttribute<AccessibleExpression> implements AccessibleExpression {

	public AttributeAccess(AccessibleExpression _object, String _name) {
		super(_object, _name);
        this.field = new FieldAccess(this.object, this.name);
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		// TODO Auto-generated method stub
		throw new SemanticsUndefinedException("Semantic getCode is undefined in AttributeAccess");
	}

}
