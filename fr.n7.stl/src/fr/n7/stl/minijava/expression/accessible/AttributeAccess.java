package fr.n7.stl.minijava.expression.accessible;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.instruction.declaration.VariableDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.declaration.AttributeDeclaration;
import fr.n7.stl.minijava.expression.AbstractAttribute;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;

public class AttributeAccess extends AbstractAttribute<AccessibleExpression> implements AccessibleExpression {

	public AttributeAccess(AccessibleExpression _object, String _name) {
		super(_object, _name);
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		/// EDITED
		return true;
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		/// EDITED
		if (((HierarchicalScope<Declaration>) _scope).knows(this.name)) {
			Declaration _declaration = _scope.get(this.name);

			// System.out.println(_declaration.getClass().toString());
			if (_declaration instanceof AttributeDeclaration) {
				this.attribute = ((AttributeDeclaration) _declaration);
				return true;
			} else {
				Logger.error("The declaration for " + this.name + " is of the wrong kind.");
				return false;
			}
		} else {
			Logger.error("The identifier " + this.name + " has not been found.");
			return false;
		}
	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		throw new SemanticsUndefinedException("getType in AttributeAccess");

	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		// TODO Auto-generated method stub
		throw new SemanticsUndefinedException("getCode in AttributeAccess");

	}

}
