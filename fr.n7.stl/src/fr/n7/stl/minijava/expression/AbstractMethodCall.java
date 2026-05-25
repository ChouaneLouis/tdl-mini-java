package fr.n7.stl.minijava.expression;

import java.util.Iterator;
import java.util.List;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.Expression;
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

public abstract class AbstractMethodCall<ObjectKind extends Expression> implements Expression {

	protected String name;

	protected MethodDeclaration declaration;

	protected ObjectKind target;

	protected List<AccessibleExpression> arguments;

	public AbstractMethodCall(ObjectKind _target, String _name, List<AccessibleExpression> _arguments) {
		this.target = _target;
		this.name = _name;
		this.arguments = _arguments;
	}

	public AbstractMethodCall(String _name, List<AccessibleExpression> _arguments) {
		this(null, _name, _arguments);
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		boolean isValid = true;
		if (this.target != null) {
			isValid = isValid && this.target.collectAndPartialResolve(_scope);
		}
		for (AccessibleExpression arg : this.arguments) {
			isValid = isValid && arg.collectAndPartialResolve(_scope);
		}
		return isValid;
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		boolean isValid = true;
		if (this.target != null) {
			isValid = isValid && this.target.completeResolve(_scope);
		}
		for (AccessibleExpression arg : this.arguments) {
			isValid = isValid && arg.completeResolve(_scope);
		}
		
		fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration classDecl = null;
		if (this.target != null) {
			Type targetType = this.target.getType();
			if (targetType instanceof fr.n7.stl.minijava.ast.type.ClassType) {
				// J'utilise getDeclaration() au lieu d'une recherche par nom pour être sûr de chopper la classe et pas autre chose (ex: un constructeur)
				classDecl = ((fr.n7.stl.minijava.ast.type.ClassType)targetType).getDeclaration();
			}
		} else {
			Declaration thisDecl = _scope.get("this");
			if (thisDecl != null && thisDecl.getType() instanceof fr.n7.stl.minijava.ast.type.ClassType) {
				classDecl = ((fr.n7.stl.minijava.ast.type.ClassType)thisDecl.getType()).getDeclaration();
			}
		}
		
		if (classDecl != null) {
			// Boucle while super importante pour l'héritage ! On cherche la méthode dans la classe, puis dans son ancêtre si pas trouvée.
			while (classDecl != null) {
				for (fr.n7.stl.minijava.ast.type.declaration.ClassElement element : classDecl.getElements()) {
					if (element instanceof MethodDeclaration && element.getName().equals(this.name)) {
						this.declaration = (MethodDeclaration) element;
						return isValid;
					}
				}
				if (classDecl.getAncestor() != null) {
					Declaration ancestorDecl = _scope.get(classDecl.getAncestor());
					if (ancestorDecl instanceof fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration) {
						classDecl = (fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration) ancestorDecl;
					} else {
						classDecl = null;
					}
				} else {
					classDecl = null;
				}
			}
		}
		
		fr.n7.stl.util.Logger.error("Method " + this.name + " not found or invalid target.");
		return false;
	}

	@Override
	public Type getType() {
		if (this.declaration != null) {
			return this.declaration.getType();
		}
		return null;
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
