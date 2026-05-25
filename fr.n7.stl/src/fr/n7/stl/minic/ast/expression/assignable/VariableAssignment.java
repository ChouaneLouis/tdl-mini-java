/**
 * 
 */
package fr.n7.stl.minic.ast.expression.assignable;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.AbstractIdentifier;
import fr.n7.stl.minic.ast.instruction.declaration.VariableDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;

/**
 * Abstract Syntax Tree node for an expression whose computation assigns a variable.
 * @author Marc Pantel
 *
 */
public class VariableAssignment extends AbstractIdentifier implements AssignableExpression {
	
	protected VariableDeclaration declaration;
	protected fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration classDeclaration;
	protected fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration parameterDeclaration;

	public VariableAssignment(String _name) {
		super(_name);
	}
	
	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		if (((HierarchicalScope<Declaration>)_scope).knows(this.name)) {
			Declaration _declaration = _scope.get(this.name);
			if (_declaration instanceof VariableDeclaration) {
				this.declaration = ((VariableDeclaration) _declaration);
				return true;
			} else if (_declaration instanceof fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration) {
				this.parameterDeclaration = (fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration) _declaration;
				return true;
			} else if (_declaration instanceof fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration) {
				this.classDeclaration = (fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration) _declaration;
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
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		return true;
	}
	
	@Override
	public Type getType() {
		if (this.classDeclaration != null) {
			return new fr.n7.stl.minijava.ast.type.ClassType(this.classDeclaration);
		}
		if (this.parameterDeclaration != null) {
			return this.parameterDeclaration.getType();
		}
        return this.declaration.getType();
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
        Fragment _result = _factory.createFragment();
		if (this.classDeclaration != null) {
			return _result;
		}
		if (this.parameterDeclaration != null) {
			_result.add(_factory.createLoadL(this.parameterDeclaration.getOffset()));
			return _result;
		}
        _result.add(_factory.createLoadL(this.declaration.getOffset()));
        return _result;
	}

}
