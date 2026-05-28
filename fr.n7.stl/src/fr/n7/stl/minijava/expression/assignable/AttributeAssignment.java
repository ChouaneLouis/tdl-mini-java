package fr.n7.stl.minijava.expression.assignable;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.accessible.FieldAccess;
import fr.n7.stl.minic.ast.expression.assignable.AssignableExpression;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.expression.AbstractAttribute;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;

public class AttributeAssignment extends AbstractAttribute<AssignableExpression> implements AssignableExpression {

	public AttributeAssignment(AssignableExpression _object, String _name) {
		super(_object, _name);
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		/*
		 * FieldAccess fa = new FieldAccess(name)
		 * /// EDITED : il faut remonter l'adresse de l'attribut
		 * /// But : récupérer le record et faire un getcode du fieldAccess
		 * if (this.object instanceof ThisAssignment){
		 * ThisAssignment ta = (ThisAssignment) this.object;
		 * ta.get
		 * Fragment f = _factory.createFragment();
		 * f.append(ta.getCode(_factory));
		 * this.of
		 * }
		 */
		Fragment f = _factory.createFragment();
		System.out.println(this.object.getClass().toString());

		// @ de la struct
		f.append(this.object.getCode(_factory));
		// Get offset

		// this.thisrecord

		// TODO Auto-generated method stub
		throw new SemanticsUndefinedException("Semantic getCode is undefined in AttributeAssignment");
	}

}
