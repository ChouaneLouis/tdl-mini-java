package fr.n7.stl.minijava.expression.assignable;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.assignable.AssignableExpression;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.minijava.ast.type.declaration.AttributeDeclaration;
import fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration;
import fr.n7.stl.minijava.ast.type.declaration.ClassElement;
import fr.n7.stl.minijava.expression.AbstractAttribute;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;

public class AttributeAssignment extends AbstractAttribute<AssignableExpression> implements AssignableExpression {

	public AttributeAssignment(AssignableExpression _object, String _name) {
		super(_object, _name);
	}



	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment f = _factory.createFragment();
		f.append(this.object.getCode(_factory));
		// L'objet est une expression affectable, donc son getCode() renvoie l'adresse de la variable.
		// En MiniJava, les objets sont des pointeurs. Il faut donc lire cette adresse pour récupérer le pointeur !
		f.add(_factory.createLoadI(1));
		f.add(_factory.createLoadL(this.attribute.getOffset()));
		f.add(fr.n7.stl.tam.ast.TAMFactory.createBinaryOperator(fr.n7.stl.minic.ast.expression.accessible.BinaryOperator.Add));
		return f;
	}

}
