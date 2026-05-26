package fr.n7.stl.minijava.expression.assignable;

import java.beans.ParameterDescriptor;
import java.lang.reflect.Parameter;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.accessible.BinaryOperator;
import fr.n7.stl.minic.ast.expression.assignable.AssignableExpression;
import fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration;
import fr.n7.stl.minijava.expression.AbstractThis;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.tam.ast.TAMInstruction;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;

public class ThisAssignment extends AbstractThis<AssignableExpression> implements AssignableExpression {

	public ThisAssignment() {
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		// TODO : ne fonctionne que si l'adresse de this est placé juste au dessus -> il
		// faut systématiquement load avant d'appeler ici, ou ajouter un attribut
		ParameterDeclaration pr = (ParameterDeclaration) this.declaration;
		Fragment f = _factory.createFragment();
		// On récupère l'adresse de this qui est toujours le premier paramètre de la
		// méthode
		f.add(_factory.createLoad(Register.LB, -1, 1));

		f.add(_factory.createLoadL(pr.getOffset()));
		// offset positif
		f.add(_factory.createLoadL(-1));
		f.add(TAMFactory.createBinaryOperator(BinaryOperator.Multiply));
		f.add(TAMFactory.createBinaryOperator(BinaryOperator.Add));
		return f;
	}

}