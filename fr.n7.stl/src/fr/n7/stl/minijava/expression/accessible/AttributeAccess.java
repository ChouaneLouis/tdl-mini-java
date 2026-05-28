package fr.n7.stl.minijava.expression.accessible;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.expression.accessible.BinaryOperator;
import fr.n7.stl.minic.ast.instruction.declaration.VariableDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
<<<<<<< HEAD
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.minijava.ast.type.declaration.AttributeDeclaration;
import fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration;
import fr.n7.stl.minijava.ast.type.declaration.ClassElement;
=======
import fr.n7.stl.minijava.ast.type.declaration.AttributeDeclaration;
>>>>>>> alexis_temp
import fr.n7.stl.minijava.expression.AbstractAttribute;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Library;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.tam.ast.TAMInstruction;
import fr.n7.stl.util.Logger;

public class AttributeAccess extends AbstractAttribute<AccessibleExpression> implements AccessibleExpression {

	public AttributeAccess(AccessibleExpression _object, String _name) {
		super(_object, _name);
	}

	@Override
<<<<<<< HEAD
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
=======
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
>>>>>>> alexis_temp

	}

}
