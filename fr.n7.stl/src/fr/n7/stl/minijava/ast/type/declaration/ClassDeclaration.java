/**
 * 
 */
package fr.n7.stl.minijava.ast.type.declaration;

import java.util.List;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.instruction.Instruction;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.scope.SymbolTable;
import fr.n7.stl.minic.ast.type.Type;
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

	/**
	 * 
	 */
	public ClassDeclaration(boolean _concrete, String _name, String _ancestor, List<ClassElement> _elements) {
		this.concrete = _concrete;
		this.name = _name;
		this.ancestor = _ancestor;
		this.elements = _elements;
	}

	/**
	 * 
	 */
	public ClassDeclaration(boolean _concrete, String _name, List<ClassElement> _elements) {
		this(_concrete, _name, null, _elements);
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		if (_scope.accepts(this)) {
			_scope.register(this);

			HierarchicalScope<Declaration> classScope = new SymbolTable(_scope);
			for (ClassElement classElement : elements) {
				if (classElement instanceof Declaration) {
					Declaration declaration = (Declaration) classElement;
					classScope.register(declaration);
				} else {
					Logger.error("ClassElement " + classElement.toString() + " is not a Declaration\n");
				}
			}
			return true;
		} else {
			Logger.error("class " + this.name + " is already defined.");
			return false;
		}
		// throw new SemanticsUndefinedException("Semantics collect is undefined in
		// ClassDeclaration.");
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
		throw new SemanticsUndefinedException("Semantics resolve is undefined in ClassDeclaration.");
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		//throw new SemanticsUndefinedException("Semantics resolve is undefined in ClassDeclaration.");
		if (this.ancestor != null) {
			Declaration ancestorDeclaration = _scope.get(this.ancestor);
			if (ancestorDeclaration == null) {
				Logger.error("Class " + this.name + " extends an unknown class " + this.ancestor);
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean checkType() {
		throw new SemanticsUndefinedException("Semantics check type is undefined in ClassDeclaration.");
	}

	@Override
	public int allocateMemory(Register _register, int _offset) {
		throw new SemanticsUndefinedException("Semantics allocation memory is undefined in ClassDeclaration.");
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		throw new SemanticsUndefinedException("Semantics get code is undefined in ClassDeclaration.");
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toString() {
		String image = "";
		if (!this.concrete) {
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

}
