package fr.n7.stl.minijava.expression.accessible;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minijava.expression.AbstractThis;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.type.Type;

public class ThisAccess extends AbstractThis<AccessibleExpression> implements AccessibleExpression {

	public ThisAccess() {
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
<<<<<<< HEAD
		Fragment result = _factory.createFragment();
		// 'this' est passé implicitement en premier paramètre caché lors des appels de méthode.
		// En machine TAM, il se retrouve juste en dessous des variables locales de la méthode, donc à LB - 1 !
		result.add(_factory.createLoad(Register.LB, -1, 1));
=======
		Fragment f = _factory.createFragment();
		if (this.declaration instanceof fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration) {
			int offset = ((fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration) this.declaration).getOffset();
			f.add(_factory.createLoad(Register.LB, offset, 1));
		} else {
			f.add(_factory.createLoad(Register.LB, -1, 1));
		}
		return f;
>>>>>>> alexis_temp

		return result;
	}

}
