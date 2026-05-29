/**
 * 
 */
package fr.n7.stl.minic.ast.instruction.declaration;

import java.util.Iterator;
import java.util.List;

import fr.n7.stl.minic.ast.Block;
import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.scope.SymbolTable;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;

/**
 * Abstract Syntax Tree node for a function declaration.
 * 
 * @author Marc Pantel
 */
public class FunctionDeclaration implements DeclarationInstruction {

	/**
	 * Name of the function
	 */
	protected String name;
	
	public void setName(String _name) {
		this.name = _name;
	}

	/**
	 * AST node for the returned type of the function
	 */
	protected Type type;

	/**
	 * List of AST nodes for the formal parameters of the function
	 */
	protected List<ParameterDeclaration> parameters;

	/**
	 * @return the parameters
	 */
	public List<ParameterDeclaration> getParameters() {
		return parameters;
	}

	/**
	 * AST node for the body of the function
	 */
	protected Block body;

	/**
	 * Builds an AST node for a function declaration
	 * 
	 * @param _name       : Name of the function
	 * @param _type       : AST node for the returned type of the function
	 * @param _parameters : List of AST nodes for the formal parameters of the
	 *                    function
	 * @param _body       : AST node for the body of the function
	 */
	public FunctionDeclaration(String _name, Type _type, List<ParameterDeclaration> _parameters, Block _body) {
		this.name = _name;
		this.type = _type;
		this.parameters = _parameters;
		this.body = _body;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String _result = this.type + " " + this.name + "( ";
		Iterator<ParameterDeclaration> _iter = this.parameters.iterator();
		if (_iter.hasNext()) {
			_result += _iter.next();
			while (_iter.hasNext()) {
				_result += " ," + _iter.next();
			}
		}
		return _result + " )" + this.body;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.n7.stl.block.ast.Declaration#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.n7.stl.block.ast.Declaration#getType()
	 */
	@Override
	public Type getType() {
		return this.type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.n7.stl.block.ast.instruction.Instruction#collect(fr.n7.stl.block.ast.scope
	 * .Scope)
	 */
	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		// throw new SemanticsUndefinedException( "Semantics collectAndPartialResolve is
		// undefined in FunctionDeclaration.");
		if (_scope.accepts(this)) {
			_scope.register(this);

			// Sous scope
			HierarchicalScope<Declaration> localScope = new SymbolTable(_scope);

			boolean ok = true;

			// On enregistre tous les paramètres de la fonction dans le sous scope
			for (ParameterDeclaration param : this.parameters) {
				if (localScope.accepts(param)) {
					localScope.register(param);
				} else {
					fr.n7.stl.util.Logger.error("Le paramètre " + param.getName() + " est défini plusieurs fois.");
					ok = false;
				}
			}
			ok = ok && this.body.collectAndPartialResolve(localScope, this);
			return ok;
		} else {
			fr.n7.stl.util.Logger.error("La fonction " + this.name + " est déjà définie dans ce scope.");
			return false;
		}
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
		throw new SemanticsUndefinedException(
				"Semantics collectAndPartialResolve is undefined in ConstantDeclaration.");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.n7.stl.block.ast.instruction.Instruction#resolve(fr.n7.stl.block.ast.scope
	 * .Scope)
	 */
	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		// throw new SemanticsUndefinedException("Semantics resolve is undefined in
		// FunctionDeclaration.");
		boolean ok = true;

		ok = ok && this.type.completeResolve(_scope);

		HierarchicalScope<Declaration> localScope = new SymbolTable(_scope);

		for (ParameterDeclaration param : this.parameters) {
			if (localScope.accepts(param)) {
				localScope.register(param);
			}
			// Complete resolve ajouté si jamais il faut gérer des fonctions avec des types
			// spécifiques
			// ok = ok && param.completeResolve(localScope);
		}

		ok = ok && this.body.completeResolve(localScope);

		return ok;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.n7.stl.block.ast.instruction.Instruction#checkType()
	 */
	@Override
	public boolean checkType() {
		// throw new SemanticsUndefinedException("Semantics checkType is undefined in
		// FunctionDeclaration.");
		return this.body.checkType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.n7.stl.block.ast.instruction.Instruction#allocateMemory(fr.n7.stl.tam.ast.
	 * Register, int)
	 */
	@Override
	public int allocateMemory(Register _register, int _offset) {
		// Calculate total parameter size first
		int totalParamSize = 0;
		for (ParameterDeclaration param : this.parameters) {
			totalParamSize += param.getType().length();
		}

		// Assign offsets: parameters are BELOW LB (negative side)
		// The 3 header words (static link, dynamic link, return address) sit
		// between LB and the parameters, so params start at -(totalParamSize)
		// relative to LB, going upward toward LB.
		int paramOffset = -totalParamSize;
		for (ParameterDeclaration param : this.parameters) {
			param.offset = paramOffset; // e.g. -3, then -2, then -1 for 3 words
			paramOffset += param.getType().length();
		}

		// Local variables start at LB + 3 (after the 3-word frame header)
		this.body.allocateMemory(Register.LB, 3);

		return _offset;
		/// EDITED
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.n7.stl.block.ast.instruction.Instruction#getCode(fr.n7.stl.tam.ast.
	 * TAMFactory)
	 */
	@Override
	public Fragment getCode(TAMFactory _factory) {
		// throw new SemanticsUndefinedException("Semantics getCode is undefined in
		// FunctionDeclaration.");
		Fragment _result = _factory.createFragment();

		// Create a unique label to jump past this function's body
		String skipLabel = "skip_" + this.name;

		// Jump over the function body during sequential execution
		_result.add(_factory.createJump(skipLabel));

		// The function body, prefixed with its name as the call target
		Fragment bodyCode = this.body.getCode(_factory);
		bodyCode.addPrefix(this.name);
		_result.append(bodyCode);

		// Landing point after the jump — execution resumes here
		_result.addSuffix(skipLabel);

		return _result;

		/// EDITED
	}

}
