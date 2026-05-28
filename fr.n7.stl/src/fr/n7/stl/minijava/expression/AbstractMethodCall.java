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
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration;
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
		boolean ok = this.target.completeResolve(_scope);
		if (!ok) return false;

		Type targetType = this.target.getType();
		if (!(targetType instanceof ClassType)) {
			Logger.error("L'expression n'est pas un objet. Impossible d'appeler la méthode " + this.name);
			return false;
		}

		ClassDeclaration classDecl = ((ClassType) targetType).getDeclaration();
		
		for (fr.n7.stl.minijava.ast.type.declaration.ClassElement element : classDecl.getElements()) {
			if (element instanceof MethodDeclaration) {
				MethodDeclaration md = (MethodDeclaration) element;
				if (md.getName().equals(this.name)) {
					this.declaration = md;
					break;
				}
			}
		}

		if (this.declaration == null) {
			Logger.error("La méthode " + this.name + " n'existe pas dans la classe " + classDecl.getName());
			return false;
		}

		// --- ENCAPSULATION CHECK ---
		fr.n7.stl.minijava.ast.type.declaration.AccessRight right = this.declaration.getAccessRight();
		if (right == fr.n7.stl.minijava.ast.type.declaration.AccessRight.PRIVATE || right == fr.n7.stl.minijava.ast.type.declaration.AccessRight.PROTECTED) {
			Declaration currentClassDecl = _scope.knows("$currentClass") ? _scope.get("$currentClass") : null;
			String currentClassName = null;
			if (currentClassDecl != null && currentClassDecl instanceof fr.n7.stl.minic.ast.instruction.declaration.VariableDeclaration) {
				Type t = ((fr.n7.stl.minic.ast.instruction.declaration.VariableDeclaration) currentClassDecl).getType();
				if (t instanceof ClassType) {
					currentClassName = ((ClassType) t).getDeclaration().getName();
				}
			}

			if (right == fr.n7.stl.minijava.ast.type.declaration.AccessRight.PRIVATE) {
				if (currentClassName == null || !currentClassName.equals(classDecl.getName())) {
					Logger.error("Encapsulation error: La méthode " + this.name + " est privée dans la classe " + classDecl.getName() + " et ne peut pas être appelée ici.");
					return false;
				}
			} else if (right == fr.n7.stl.minijava.ast.type.declaration.AccessRight.PROTECTED) {
				// TODO: Gérer l'héritage pour PROTECTED (pour l'instant on limite à la même classe)
				if (currentClassName == null || !currentClassName.equals(classDecl.getName())) {
					Logger.error("Encapsulation error: La méthode " + this.name + " est protégée dans la classe " + classDecl.getName() + " et ne peut pas être appelée ici.");
					return false;
				}
			}
		}
		// ---------------------------

		// Résoudre les arguments de l'appel (via le FunctionCall sous-jacent)
		if (this.declaration.getElementKind() == fr.n7.stl.minijava.ast.type.declaration.ElementKind.CLASS) {
			this.call = new fr.n7.stl.minic.ast.expression.FunctionCall(name, this.arguments);
			this.call.collectAndPartialResolve(_scope);
		}
		
		this.call.setFunction(this.declaration.getFunction());
		return this.call.completeResolve(_scope);
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
