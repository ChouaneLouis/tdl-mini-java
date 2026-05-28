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
<<<<<<< HEAD
		Fragment result = _factory.createFragment();
        
		if (this.attribute.getElementKind() == fr.n7.stl.minijava.ast.type.declaration.ElementKind.CLASS) {
			// STATIC FIELD: On pousse l'adresse de la variable globale (SB + offset)
			result.add(_factory.createLoadA(fr.n7.stl.tam.ast.Register.SB, this.attribute.getOffset()));
		} else {
			// 1. On charge l'adresse de l'objet (la référence) au sommet de la pile
			result.append(this.object.getCode(_factory));
			
			// 2. On ajoute l'offset (le décalage) de l'attribut dans la classe
			result.add(_factory.createLoadL(this.attribute.getOffset()));
			result.add(fr.n7.stl.tam.ast.Library.IAdd); 
			// L'adresse exacte de l'attribut est maintenant au sommet de la pile.
			// On ne fait PAS de LoadI car on veut l'adresse pour une affectation.
		}
        
        return result;
=======
		Fragment f = _factory.createFragment();
		f.append(this.object.getCode(_factory));
		// L'objet est une expression affectable, donc son getCode() renvoie l'adresse de la variable.
		// En MiniJava, les objets sont des pointeurs. Il faut donc lire cette adresse pour récupérer le pointeur !
		f.add(_factory.createLoadI(1));
		f.add(_factory.createLoadL(this.attribute.getOffset()));
		f.add(fr.n7.stl.tam.ast.TAMFactory.createBinaryOperator(fr.n7.stl.minic.ast.expression.accessible.BinaryOperator.Add));
		return f;
>>>>>>> alexis_temp
	}

}
