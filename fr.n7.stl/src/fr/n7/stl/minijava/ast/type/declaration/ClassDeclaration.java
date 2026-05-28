/**
 * 
 */
package fr.n7.stl.minijava.ast.type.declaration;

import java.util.ArrayList;
import java.util.List;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.instruction.Instruction;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.scope.SymbolTable;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minic.ast.type.RecordType;
import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration;
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;

/**
 * 
 */
public class ClassDeclaration implements Instruction, Declaration {
	
	protected List<ClassElement> elements;
	
	protected boolean concrete;
	
	protected String name;
	
	protected String ancestor;

    protected HierarchicalScope<Declaration> scope;

    protected List<ConstructorDeclaration> constructors;

	/**
	 * 
	 */
	public ClassDeclaration(boolean _concrete, String _name, String _ancestor, List<ClassElement> _elements) {
		this.concrete = _concrete;
		this.name = _name;
		this.ancestor = _ancestor;
		this.elements = _elements;
        this.constructors = new ArrayList<>();
	}
	
	/**
	 * 
	 */
	public ClassDeclaration(boolean _concrete, String _name, List<ClassElement> _elements) {
		this( _concrete, _name, null, _elements);
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
        /// EDITED
        if (!_scope.accepts(this)) {
            Logger.error("La classe " + this.name + " est déja définie.");
            return false;
        }
        _scope.register(this);

        this.scope = new SymbolTable(_scope);

        boolean ok = true;
        for (ClassElement classElement : this.elements) {
            if (classElement instanceof ConstructorDeclaration) {
                ConstructorDeclaration constructor = (ConstructorDeclaration) classElement;
                constructor.declareFunction(this, this.constructors.size());
                ok &= constructor.getFunction().collectAndPartialResolve(_scope);
                this.constructors.add(constructor);

            } else {
                Logger.error(classElement.getName() + " n'est pas de bon type.");
                ok = false;
            }
        }

        return ok;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
		throw new SemanticsUndefinedException( "Semantics resolve is undefined in ClassDeclaration.");
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
        /// EDITED
        boolean ok = true;
        for (ClassElement classElement : this.elements) {
            if (classElement instanceof ConstructorDeclaration) {
                ConstructorDeclaration constructor = (ConstructorDeclaration) classElement;
                ok &= constructor.getFunction().completeResolve(this.scope);
            } else {
                Logger.error(classElement.getName() + " n'est pas de bon type.");
                ok = false;
            }
        }

        return ok;
	}

	@Override
	public boolean checkType() {
        /// EDITED
        boolean ok = true;
        for (ClassElement classElement : this.elements) {
            if (classElement instanceof ConstructorDeclaration) {
                ConstructorDeclaration constructor = (ConstructorDeclaration) classElement;
                ok &= constructor.getFunction().checkType();
            } else {
                Logger.error(classElement.getName() + " n'est pas de bon type.");
                ok = false;
            }
        }

        return ok;
	}

	@Override
	public int allocateMemory(Register _register, int _offset) {
        /// EDITED
        int offset = _offset;
        for (ClassElement classElement : this.elements) {
            if (classElement instanceof ConstructorDeclaration) {
                ConstructorDeclaration constructor = (ConstructorDeclaration) classElement;
                offset = constructor.getFunction().allocateMemory(_register, offset);
            } else {
                Logger.error(classElement.getName() + " n'est pas de bon type.");
            }
        }

        return offset;
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
        Fragment _result = _factory.createFragment();
        for (ClassElement classElement : this.elements) {
            if (classElement instanceof ConstructorDeclaration) {
                ConstructorDeclaration constructor = (ConstructorDeclaration) classElement;
                _result.append(constructor.getFunction().getCode(_factory));
            } else {
                Logger.error(classElement.getName() + " n'est pas de bon type.");
            }
        }

        return _result;
        /// EDITED
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Type getType() {
		return new ClassType(this);
	}
	
	@Override
	public String toString() {
		String image = "";
		if (! this.concrete) {
			image += "abstract ";
		}
		image += "class " + this.name + " ";
		if (this.ancestor != null) {
			image += "extends " + this.ancestor + " ";
		}
		image += "{\n";
		for (ClassElement e : this.elements) {
			image += e;
		}
		image += "}\n";
		return image;
	}

    public RecordType getRecordType() {
        return new RecordType("_class_" + this.name, new ArrayList<>());
    }

    public String getConstructorName(List<AccessibleExpression> parameters) {
        List<Type> parameterTypes = parameters.stream()
            .map(AccessibleExpression::getType)
            .toList();
        for (ConstructorDeclaration constructor : this.constructors) {
            List<Type> signature  = new ArrayList<>(constructor.getFunction().getParameters().stream()
                .map(ParameterDeclaration::getType)
                .toList());
            signature.remove(0);
            if (parameterTypes.size() != signature.size()) continue;
            boolean ok = true;
            for (int i = 0; i < parameterTypes.size(); i++) {
                ok &= parameterTypes.get(i).compatibleWith(signature.get(i));
            }
            if (ok) return constructor.getName();
        }
        return null;
    }

}
