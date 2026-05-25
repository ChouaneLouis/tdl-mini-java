package fr.n7.stl.minijava.expression.accessible;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.minijava.ast.type.declaration.AttributeDeclaration;
import fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration;
import fr.n7.stl.minijava.ast.type.declaration.ClassElement;
import fr.n7.stl.minijava.expression.AbstractAttribute;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Library;
import fr.n7.stl.tam.ast.TAMFactory;

public class AttributeAccess extends AbstractAttribute<AccessibleExpression> implements AccessibleExpression {

	public AttributeAccess(AccessibleExpression _object, String _name) {
		super(_object, _name);
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment result = _factory.createFragment();
        
		if (this.attribute.getElementKind() == fr.n7.stl.minijava.ast.type.declaration.ElementKind.CLASS) {
			// STATIC FIELD
			result.add(_factory.createLoad(fr.n7.stl.tam.ast.Register.SB, this.attribute.getOffset(), this.attribute.getType().length()));
		} else {
			// INSTANCE FIELD
			// 1. On charge l'adresse de l'objet (la référence) au sommet de la pile
			result.append(this.object.getCode(_factory));
			
			// 2. On ajoute l'offset (le décalage) de l'attribut dans la classe
			result.add(_factory.createLoadL(this.attribute.getOffset()));
			result.add(Library.IAdd); 
			
			// 3. On lit la mémoire à cette adresse (Load Indirect) de la taille de l'attribut
			result.add(_factory.createLoadI(this.getType().length()));
		}
        
        return result;

	}

}
