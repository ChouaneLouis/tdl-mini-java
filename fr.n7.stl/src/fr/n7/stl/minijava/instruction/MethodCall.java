package fr.n7.stl.minijava.instruction;

import java.util.Iterator;
import java.util.List;

import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.instruction.Instruction;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minijava.ast.type.declaration.MethodDeclaration;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;

/**
 * Appel de méthode utilisé comme une instruction (seul sur sa ligne).
 * Ex: a.setV(9);
 * 
 * La grosse différence avec MethodCallAccess, c'est qu'ici on se fiche de
 * la valeur de retour. Donc le getCode va appeler la méthode, puis POP
 * la valeur de retour de la pile pour la garder propre.
 */
public class MethodCall implements Instruction {

	protected AccessibleExpression target;
	protected String name;
	protected MethodDeclaration method;

	// On s'appuie sur le FunctionCall de miniC
	protected fr.n7.stl.minic.ast.expression.FunctionCall call;
	protected List<AccessibleExpression> arguments;

	// true si on a retiré "this" pour un appel statique
	protected boolean isStaticRebuilt = false;

	public MethodCall(AccessibleExpression _target, String _name, List<AccessibleExpression> _arguments) {
		this.name = _name;
		this.method = null;
		this.target = _target;
		if (this.target == null) {
			this.target = new fr.n7.stl.minijava.expression.accessible.ThisAccess();
		}
		this.arguments = _arguments;
		
		java.util.List<AccessibleExpression> allArgs = new java.util.LinkedList<>();
		allArgs.add(this.target);
		allArgs.addAll(this.arguments);
		this.call = new fr.n7.stl.minic.ast.expression.FunctionCall(name, allArgs);
	}

	public MethodCall(String _name, List<AccessibleExpression> _arguments) {
		this(null, _name, _arguments);
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		if (this.target != null) {
			String targetName = this.target.toString().trim();
			if (_scope.knows(targetName)) {
				Declaration decl = _scope.get(targetName);
				if (decl instanceof fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration) {
					// Appel statique détecté (la cible est un nom de classe)
					this.call = new fr.n7.stl.minic.ast.expression.FunctionCall(name, this.arguments);
					this.isStaticRebuilt = true;
				}
			}
		}
		return this.call.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
		return this.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		Declaration d = _scope.get(name);
		if (d instanceof MethodDeclaration) {
			this.method = (MethodDeclaration) d;
			// Si la méthode est statique mais qu'on ne l'avait pas vu avant
			if (this.method.getElementKind() == fr.n7.stl.minijava.ast.type.declaration.ElementKind.CLASS) {
				if (!this.isStaticRebuilt) {
					this.call = new fr.n7.stl.minic.ast.expression.FunctionCall(name, this.arguments);
					this.call.collectAndPartialResolve(_scope);
					this.isStaticRebuilt = true;
				}
			}
		}
		return this.call.completeResolve(_scope);
	}

	@Override
	public boolean checkType() {
		return true; // Délégué au FunctionCall qui fait confiance pour l'instant
	}

	@Override
	public int allocateMemory(Register _register, int _offset) {
		// Pas d'allocation mémoire pour une instruction d'appel
		return _offset;
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment f = this.call.getCode(_factory);
		// Crucial : si la méthode renvoie un truc, on le dépile car on est une instruction !
		// Sinon la pile va fuir (stack overflow).
		if (this.call.getType().length() > 0) {
			f.add(_factory.createPop(0, this.call.getType().length()));
		}
		return f;
	}

	@Override
	public String toString() {
		String image = "";
		if (this.target != null) {
			image += target + ".";
		}
		image += this.name;
		image += "( ";
		Iterator<AccessibleExpression> iterator = this.arguments.iterator();
		if (iterator.hasNext()) {
			AccessibleExpression argument = iterator.next();
			image += argument;
			while (iterator.hasNext()) {
				argument = iterator.next();
				image += " ," + argument;
			}
		}
		image += ");\n";
		return image;
	}

}
