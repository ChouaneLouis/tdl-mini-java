package fr.n7.stl.minijava.ast.type.declaration;

import java.util.List;

import fr.n7.stl.minic.ast.Block;
import fr.n7.stl.minic.ast.instruction.Instruction;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;

/**
 * La déclaration de la classe Main (le point d'entrée du programme).
 * 
 * En MiniJava, le Main est un peu spécial, il ressemble à un mix entre
 * une classe et une grosse fonction. On y trouve des déclarations (attributs)
 * et le bloc principal `public static void main(String[] args)`.
 */
public class MainDeclaration implements Instruction {

	protected String name;

	// Liste des déclarations globales du Main (variables, etc.)
	protected List<Declaration> declarations;

	// Le bloc de code du main()
	protected Block main;

	public MainDeclaration(String _name, List<Declaration> _declarations, Block _main) {
		this.name = _name;
		this.declarations = _declarations;
		this.main = _main;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
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
		// On collecte aussi le bloc principal
		ok = ok && this.main.collectAndPartialResolve(_scope);
		return ok;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
		return this.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
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
		// Le bloc main alloue sa propre mémoire, on ne décale pas l'offset global
		this.main.allocateMemory(_register, _offset);
		return _offset;
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment fragment = _factory.createFragment();

		// On parcourt tous les éléments définis dans le Main
		for (Declaration d : this.declarations) {
			// S'il y a des instructions exécutables déclarées au niveau global, on génère leur code
			if (d instanceof Instruction) {
				Instruction executableElement = (Instruction) d;
				fragment.append(executableElement.getCode(_factory));
			}
		}

		// On ajoute le code du bloc principal
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
