package fr.n7.stl.minijava.ast.type.declaration;

import java.util.List;

import fr.n7.stl.minic.ast.Block;
import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.instruction.Instruction;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
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
		// throw new SemanticsUndefinedException("Semantics collectAndPartialResolve is
		// undefined in MainDeclaration.");
		boolean ok = true;
		for (Declaration declaration : declarations) {

			if (declaration instanceof Instruction) {
				ok = ok && ((Instruction) declaration).collectAndPartialResolve(_scope);
			} else {
				if (_scope.accepts(declaration)) {
					_scope.register(declaration);
				} else {
					ok = false;
				}
			}

		}
		ok = ok && this.main.collectAndPartialResolve(_scope);
		return ok;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
		// TODO Auto-generated method stub
		// throw new SemanticsUndefinedException("Semantics collectAndPartialResolve is
		// undefined in MainDeclaration.");
		return this.collectAndPartialResolve(_scope);

	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		// TODO Auto-generated method stub
		// throw new SemanticsUndefinedException("Semantics completeResolve is undefined
		// in MainDeclaration.");
		boolean isValid = true;

		for (Declaration declaration : this.declarations) {
			if (declaration instanceof Instruction) {
				isValid = isValid && ((Instruction) declaration).completeResolve(_scope);
			}
		}

		isValid = isValid && this.main.completeResolve(_scope);

		return isValid;

	}

	@Override
	public boolean checkType() {
		// TODO Auto-generated method stub
		// throw new SemanticsUndefinedException("Semantics checktype is undefined in
		// MainDeclaration.");
		boolean ok = true;
		for (Declaration d : declarations) {
			if (d instanceof Instruction)
				ok = ok && ((Instruction) d).checkType();
		}
		ok = ok && this.main.checkType();
		return ok;

	}

	@Override
	public int allocateMemory(Register _register, int _offset) {
		// TODO Auto-generated method stub
		// throw new SemanticsUndefinedException("Semantics allocatememory is undefined
		// in MainDeclaration.");
		this.main.allocateMemory(_register, _offset);
		return _offset;

	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		// TODO Auto-generated method stub
		// throw new SemanticsUndefinedException("Semantics getcode is undefined in
		// MainDeclaration.");
		Fragment fragment = _factory.createFragment();

		// On parcourt tous les éléments définis dans la classe
		for (Declaration d : this.declarations) {

			// Seules les méthodes et les constructeurs (qui implémentent Instruction)
			// ont du code exécutable à générer. Les attributs sont ignorés ici.
			if (d instanceof Instruction) {
				Instruction executableElement = (Instruction) d;

				// On récupère le fragment de code de la méthode/constructeur
				// et on l'ajoute au fragment global de la classe
				fragment.append(executableElement.getCode(_factory));
			}
		}

		fragment.append(this.main.getCode(_factory));

		return fragment;

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
