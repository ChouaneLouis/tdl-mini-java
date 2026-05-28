package fr.n7.stl.minijava.ast.type.declaration;

import java.util.List;

import fr.n7.stl.minic.ast.Block;
import fr.n7.stl.minic.ast.instruction.Instruction;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.instruction.declaration.DeclarationInstruction;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;

public class MainDeclaration implements Instruction {
	
	protected String name;
	
	protected List<Declaration> declarations;
	
	protected Block main;

	public MainDeclaration(String _name, List<Declaration> _declarations, Block _main) {
		this.name = _name;
		this.declarations = _declarations;
		this.main = _main;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
        /// EDITED
        boolean ok = true;
        for (Declaration declaration : this.declarations) {
            if (declaration instanceof DeclarationInstruction) {
                ok &= ((DeclarationInstruction) declaration).collectAndPartialResolve(_scope);
            } else if (_scope.accepts(declaration)) {
                _scope.register(declaration);
            } else {
                ok = false;
            }
        }
        return ok && this.main.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
        /// EDITED
        return this.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
        /// EDITED
        boolean ok = true;
        for (Declaration declaration : this.declarations) {
            if (declaration instanceof DeclarationInstruction) {
                ok &= ((DeclarationInstruction) declaration).completeResolve(_scope);
            }
        }
		return ok && this.main.completeResolve(_scope);
	}

	@Override
	public boolean checkType() {
        /// EDITED
        boolean ok = true;
        for (Declaration declaration : this.declarations) {
            if (declaration instanceof DeclarationInstruction) {
                ok &= ((DeclarationInstruction) declaration).checkType();
            }
        }
		return ok && this.main.checkType();
	}

	@Override
	public int allocateMemory(Register _register, int _offset) {
        /// EDITED
        int offset = _offset;
        for (Declaration declaration : this.declarations) {
            if (declaration instanceof DeclarationInstruction) {
                offset = ((DeclarationInstruction) declaration).allocateMemory(_register, offset);
            }
        }
        this.main.allocateMemory(_register, offset);
		return offset;
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
        Fragment _result = _factory.createFragment();
        for (Declaration declaration : this.declarations) {
            if (declaration instanceof DeclarationInstruction) {
                _result.append(((DeclarationInstruction) declaration).getCode(_factory));
            }
        }
        _result.append(this.main.getCode(_factory));
        return _result;
        /// EDITED
	}
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public String toString() {
		String image = "";
		image += "public class " + this.name + " ";
		image += "{\n";
		image += "\n";
		for (Declaration uneDeclaration : this.declarations) {
			image += uneDeclaration;
			image += "\n";
		}
		image += "\tpublic static void Main( String[] args) ";
		image += this.main;
		image += "\n";
		image += "}\n";
		return image;
	}

}
