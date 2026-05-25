/**
 * 
 */
package fr.n7.stl.minic.ast.instruction;

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
public class Iteration implements Instruction {

    protected Expression condition;
    protected Block body;

    public Iteration(Expression _condition, Block _body) {
        this.condition = _condition;
        this.body = _body;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "while (" + this.condition + " )" + this.body;
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
                && this.body.collectAndPartialResolve(_scope);
    }

    @Override
    public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
        return this.condition.collectAndPartialResolve(_scope)
                && this.body.collectAndPartialResolve(_scope, _container);
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
                && this.body.completeResolve(_scope);
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.n7.stl.block.ast.Instruction#checkType()
     */
    @Override
    public boolean checkType() {
        boolean condition = this.condition.getType().compatibleWith(AtomicType.BooleanType);
        boolean body = this.body.checkType();
        if (!condition) {
            Logger.error("Type mismatch in " + this.condition +
                    " Condition : expected boolean but got "
                    + (this.condition != null ? this.condition.getType() : "null"));
        }
        return condition && body;

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
        this.body.allocateMemory(_register, _offset);
        return _offset;
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
        String condLabel = "while_" + _id + "_cond";
        String endLabel = "while_" + _id + "_end";

        _result.add(_factory.createPop(0, 0));
        _result.addSuffix(condLabel);
        _result.append(this.condition.getCode(_factory));
        _result.add(_factory.createJumpIf(endLabel, 0));
        _result.append(this.body.getCode(_factory));
        _result.add(_factory.createJump(condLabel));
        _result.addSuffix(endLabel);

        return _result;
    }

}
