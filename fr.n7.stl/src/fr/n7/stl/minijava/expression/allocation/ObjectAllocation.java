package fr.n7.stl.minijava.expression.allocation;

import java.util.Iterator;
import java.util.List;

import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.expression.assignable.AssignableExpression;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;

public class ObjectAllocation  implements AccessibleExpression, AssignableExpression {
	
	protected String name;
	
	protected List<AccessibleExpression> arguments;

    protected ClassDeclaration classDeclaration;

	public ObjectAllocation(String _name, List<AccessibleExpression> _arguments) {
		this.name = _name;
		this.arguments = _arguments;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
        /// EDITED
        if (!_scope.knows(this.name) || !(_scope.get(this.name) instanceof ClassDeclaration)) {
            Logger.error("The class " + this.name + " isn't defined.");
            return false;
        }
        this.classDeclaration = (ClassDeclaration) _scope.get(this.name);
        boolean ok = true;
        for (AccessibleExpression accessibleExpression : this.arguments) {
            ok &= accessibleExpression.collectAndPartialResolve(_scope);
        }
        
		return ok;
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
        /// EDITED
        boolean ok = true;
        for (AccessibleExpression accessibleExpression : this.arguments) {
            ok &= accessibleExpression.completeResolve(_scope);
        }
        
		return ok;
	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String toString() {
		String image = "";
		image += "new " + this.name + "( ";
		Iterator<AccessibleExpression> iterator = this.arguments.iterator();
		if (iterator.hasNext()) {
			AccessibleExpression argument = iterator.next();
			image += argument;
			while (iterator.hasNext()) {
				 argument = iterator.next();
				 image += " ," + argument;
			}
		}
		image += ")";
		return image;
	}

}
