/**
 * 
 */
package fr.n7.stl.minic.ast.expression.accessible;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.AbstractField;
import fr.n7.stl.minic.ast.expression.Expression;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;

/**
 * Implementation of the Abstract Syntax Tree node for accessing a field in a record.
 * @author Marc Pantel
 *
 */
public class FieldAccess extends AbstractField<AccessibleExpression> implements AccessibleExpression {

	/**
	 * Construction for the implementation of a record field access expression Abstract Syntax Tree node.
	 * @param _record Abstract Syntax Tree for the record part in a record field access expression.
	 * @param _name Name of the field in the record field access expression.
	 */
	public FieldAccess(AccessibleExpression _record, String _name) {
		super(_record, _name);
	}

	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.Expression#getCode(fr.n7.stl.tam.ast.TAMFactory)
	 */
	@Override
	public Fragment getCode(TAMFactory _factory) {
        Fragment _result = _factory.createFragment();
        _result.append(this.record.getCode(_factory));
        _result.add(_factory.createPop(
            0,
            this.record.getType().length() - this.field.getOffset() - this.field.getType().length()
        ));
        _result.add(_factory.createPop(
            this.field.getType().length(),
            this.field.getOffset()
        ));
        return _result;
        /// EDITED
	}

}
