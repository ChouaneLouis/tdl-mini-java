package fr.n7.stl.minijava.expression;

import java.util.Iterator;
import java.util.List;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.Expression;
import fr.n7.stl.minic.ast.expression.FunctionCall;
import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.instruction.Instruction;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.declaration.MethodDeclaration;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;

//Doit laisser la valeur de retour sur la pile Type a = methode.call()

public abstract class AbstractMethodCall<ObjectKind extends Expression> implements Expression {

	protected String name;

	protected MethodDeclaration declaration;

	protected ObjectKind target;

	protected List<AccessibleExpression> arguments;

	// Vérifier que ses attributs sont màj
	protected FunctionCall call;

	public AbstractMethodCall(ObjectKind _target, String _name, List<AccessibleExpression> _arguments) {
		this.target = _target;
		if (this.target == null) {
			this.target = (ObjectKind) new fr.n7.stl.minijava.expression.accessible.ThisAccess();
		}
		this.name = _name;
		this.arguments = _arguments;

		java.util.List<AccessibleExpression> allArgs = new java.util.LinkedList<>();
		allArgs.add((AccessibleExpression) this.target);
		allArgs.addAll(this.arguments);

		this.call = new FunctionCall(name, allArgs);
	}

	public AbstractMethodCall(String _name, List<AccessibleExpression> _arguments) {
		this(null, _name, _arguments);
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		return this.call.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		Declaration d = _scope.get(name);
		if (d instanceof MethodDeclaration) {
			this.declaration = (MethodDeclaration) d;
			if (this.declaration.getElementKind() == fr.n7.stl.minijava.ast.type.declaration.ElementKind.CLASS) {
				this.call = new fr.n7.stl.minic.ast.expression.FunctionCall(name, this.arguments);
				this.call.collectAndPartialResolve(_scope);
			}
		}
		boolean ok = this.call.completeResolve(_scope);
		return ok;
	}

	@Override
	public Type getType() {
		/// EDITED
		return this.declaration.getType();

	}

	@Override
	public String toString() {
		String image = "";
		if (this.target != null) {
			image += this.target + ".";
		}
		image += this.name + "( ";
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
