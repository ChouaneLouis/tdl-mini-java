/**
 * 
 */
package fr.n7.stl.minic.ast.expression;

import java.util.Iterator;
import java.util.List;

import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;

/**
 * Abstract Syntax Tree node for a function call expression.
 * 
 * @author Marc Pantel
 *
 */
public class FunctionCall implements AccessibleExpression {

	/**
	 * Name of the called function.
	 * TODO : Should be an expression.
	 */
	protected String name;

	/**
	 * Declaration of the called function after name resolution.
	 * TODO : Should rely on the VariableUse class.
	 */
	protected FunctionDeclaration function;

	/**
	 * List of AST nodes that computes the values of the parameters for the function
	 * call.
	 */
	protected List<AccessibleExpression> arguments;

	/**
	 * @param _name      : Name of the called function.
	 * @param _arguments : List of AST nodes that computes the values of the
	 *                   parameters for the function call.
	 */
	public FunctionCall(String _name, List<AccessibleExpression> _arguments) {
		this.name = _name;
		this.function = null;
		this.arguments = _arguments;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String _result = ((this.function == null) ? this.name : this.function) + "( ";
		Iterator<AccessibleExpression> _iter = this.arguments.iterator();
		if (_iter.hasNext()) {
			_result += _iter.next();
		}
		while (_iter.hasNext()) {
			_result += " ," + _iter.next();
		}
		return _result + ")";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.n7.stl.block.ast.expression.Expression#collect(fr.n7.stl.block.ast.scope.
	 * HierarchicalScope)
	 */
	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		// throw new SemanticsUndefinedException( "Semantics collect is undefined in
		// FunctionCall.");
		boolean ok = true;
		for (AccessibleExpression arg : this.arguments) {
			ok = ok && arg.collectAndPartialResolve(_scope);
		}
		return ok;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.n7.stl.block.ast.expression.Expression#resolve(fr.n7.stl.block.ast.scope.
	 * HierarchicalScope)
	 */
	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		// throw new SemanticsUndefinedException("Semantics resolve is undefined in
		// FunctionCall.");
		boolean ok = true;

		for (AccessibleExpression arg : this.arguments) {
			ok = ok && arg.completeResolve(_scope);
		}

		if (_scope.knows(this.name)) {
			Declaration decl = _scope.get(this.name);

			if (decl instanceof FunctionDeclaration) {
				this.function = (FunctionDeclaration) decl;
			} else {
				fr.n7.stl.util.Logger.error("L'identifiant '" + this.name + "' n'est pas une fonction.");
				ok = false;
			}
		} else {
			fr.n7.stl.util.Logger.error("La fonction '" + this.name + "' n'est pas définie.");
			ok = false;
		}

		return ok;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.n7.stl.block.ast.Expression#getType()
	 */
	@Override
	public Type getType() {
		// throw new SemanticsUndefinedException("Semantics getType is undefined in
		// FunctionCall.");
		return this.function.getType();
			}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.n7.stl.block.ast.Expression#getCode(fr.n7.stl.tam.ast.TAMFactory)
	 */
	@Override
	public Fragment getCode(TAMFactory _factory) {
		// throw new SemanticsUndefinedException("Semantics getCode is undefined in
		// FunctionCall.");
		Fragment _result = _factory.createFragment();

		for (AccessibleExpression arg : this.arguments) {
			_result.append(arg.getCode(_factory));
		}

		_result.add(_factory.createCall(this.function.getName(), Register.SB));

		return _result;
			}

}
