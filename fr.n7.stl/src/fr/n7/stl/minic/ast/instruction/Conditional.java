/**
 * 
 */
package fr.n7.stl.minic.ast.instruction;

import java.util.Optional;

import fr.n7.stl.minic.ast.Block;
import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.Expression;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.AtomicType;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;

/**
 * Implementation of the Abstract Syntax Tree node for a conditional
 * instruction.
 * 
 * @author Marc Pantel
 *
 */
public class Conditional implements Instruction {

	protected Expression condition;
	protected Block thenBranch;
	protected Block elseBranch;

	public Conditional(Expression _condition, Block _then, Block _else) {
		this.condition = _condition;
		this.thenBranch = _then;
		this.elseBranch = _else;
	}

	public Conditional(Expression _condition, Block _then) {
		this.condition = _condition;
		this.thenBranch = _then;
		this.elseBranch = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "if (" + this.condition + " )" + this.thenBranch
				+ ((this.elseBranch != null) ? (" else " + this.elseBranch) : "");
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
		return this.condition.collectAndPartialResolve(_scope)
				&& this.thenBranch.collectAndPartialResolve(_scope)
				&& (this.elseBranch == null || this.elseBranch.collectAndPartialResolve(_scope));
		/// EDITED
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.n7.stl.block.ast.instruction.Instruction#collect(fr.n7.stl.block.ast.scope
	 * .Scope)
	 */
	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
		return this.condition.collectAndPartialResolve(_scope)
				&& this.thenBranch.collectAndPartialResolve(_scope, _container)
				&& (this.elseBranch == null || this.elseBranch.collectAndPartialResolve(_scope, _container));
		/// EDITED
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
		return this.condition.completeResolve(_scope)
				&& this.thenBranch.completeResolve(_scope)
				&& (this.elseBranch == null || this.elseBranch.completeResolve(_scope));
		/// EDITED
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.n7.stl.block.ast.Instruction#checkType()
	 */
	@Override
	public boolean checkType() {
		boolean condition = this.condition.getType().compatibleWith(AtomicType.BooleanType);
		boolean thenBlock = this.thenBranch.checkType();
		boolean elseBlock = (this.elseBranch == null || this.elseBranch.checkType());
		if (!condition) {
			Logger.error("Type mismatch in " + this.condition +
					" Condition : expected boolean but got "
					+ (this.condition != null ? this.condition.getType() : "null"));
		} else if (!thenBlock) {
			Logger.error("Type mismatch in then block");
		} else if (!elseBlock) {
			Logger.error("Type mismatch in else block");
		}
		return condition && thenBlock && elseBlock;
		/// EDITED
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.n7.stl.block.ast.Instruction#allocateMemory(fr.n7.stl.tam.ast.Register,
	 * int)
	 */
	@Override
	public int allocateMemory(Register _register, int _offset) {
		this.thenBranch.allocateMemory(_register, _offset);
		if (this.elseBranch != null) {
			this.elseBranch.allocateMemory(_register, _offset);
		}
		return _offset;
		/// EDITED
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.n7.stl.block.ast.Instruction#getCode(fr.n7.stl.tam.ast.TAMFactory)
	 */
	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment _result = _factory.createFragment();
		int _id = _factory.createLabelNumber();
		String elseLabel = "if_" + _id + "_else";
		String endLabel = "if_" + _id + "_end";
		_result.append(this.condition.getCode(_factory));

		if (this.elseBranch == null) {
			_result.add(_factory.createJumpIf(endLabel, 0));
			_result.append(this.thenBranch.getCode(_factory));
		} else {
			_result.add(_factory.createJumpIf(elseLabel, 0));
			_result.append(this.thenBranch.getCode(_factory));
			_result.add(_factory.createJump(endLabel));

			_result.addSuffix(elseLabel);
			_result.append(this.elseBranch.getCode(_factory));
		}
		// _result.add(_factory.createPop(0,0)); // permet d'éviter des pb de partage de
		// reference
		// Si le bloc fini par un "print xx;" alors le suffixe est ajouté sur tous les
		// print
		// du code. Ceci n'est plus nécéssaire le jour ou le printer utilise la factory
		// pour
		// créer une instance séparé ? ou les blocs pop la mémoire alloué.
		_result.addSuffix(endLabel);

		return _result;
		/// EDITED
	}

}
