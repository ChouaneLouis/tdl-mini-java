package fr.n7.stl.minijava.expression.allocation;

import java.util.Iterator;
import java.util.List;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.expression.assignable.AssignableExpression;
import fr.n7.stl.minic.ast.instruction.Instruction;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.RecordType;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Library;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.tam.ast.TAMInstruction;
import fr.n7.stl.util.Logger;

public class ObjectAllocation implements AccessibleExpression, AssignableExpression {

	protected String name;

	protected List<AccessibleExpression> arguments;

	protected ClassType classType;

	public ObjectAllocation(String _name, List<AccessibleExpression> _arguments) {
		this.name = _name;
		this.arguments = _arguments;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		// TODO Auto-generated method stub
		// throw new SemanticsUndefinedException("collectAndPartialResolve in
		// ObjectAllocation");
		boolean ok = true;
		for (AccessibleExpression accessibleExpression : arguments) {
			if (accessibleExpression instanceof Instruction) {
				ok = ok && ((Instruction) accessibleExpression).collectAndPartialResolve(_scope);
			}
		}
		return ok;

	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		// TODO Auto-generated method stub
		// throw new SemanticsUndefinedException("completeResolve in ObjectAllocation");
		boolean ok = true;

		this.classType = new ClassType(this.name);
		ok = ok && this.classType.completeResolve(_scope);

		for (AccessibleExpression accessibleExpression : arguments) {
			if (accessibleExpression instanceof Instruction) {
				ok = ok && ((Instruction) accessibleExpression).completeResolve(_scope);
			}
		}
		return ok;

	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		// throw new SemanticsUndefinedException("getType in ObjectAllocation");
		return this.classType != null ? this.classType : new ClassType(name);

	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		// TODO Auto-generated method stub
		// throw new SemanticsUndefinedException("getCode in ObjectAllocation");
		Fragment fragment = _factory.createFragment();

		// Récupération de la déclaration de la classe pour connaître sa taille et son
		// constructeur
		ClassDeclaration classDecl = this.classType.getDeclaration();
		int objectSize = classDecl.getObjectSize();

		// Étape 1 : Allouer l'espace pour l'objet sur le Tas (Heap)
		// 1.1 On charge la taille nécessaire sur la pile
		fragment.add(_factory.createLoadL(objectSize));
		// 1.2 On appelle MAlloc. TAM retire la taille et la remplace par l'adresse
		// mémoire allouée (le pointeur 'this')
		fragment.add(Library.MAlloc);

		// Étape 2 : Empiler les arguments du constructeur
		// On duplique d'abord l'adresse de l'objet car le CALL du constructeur va
		// consommer
		// l'adresse 'this', mais l'expression 'new' entière doit laisser l'adresse
		// finale sur la pile !
		fragment.add(_factory.createPush(1)); // Réserve 1 case pour la copie
		fragment.add(_factory.createLoad(fr.n7.stl.tam.ast.Register.ST, -2, 1)); // Copie l'adresse située
																					// juste en dessous

		// On génère et empile le code de chaque argument
		for (AccessibleExpression arg : this.arguments) {
			fragment.append(arg.getCode(_factory));
		}

		// Étape 3 : Appeler le constructeur de la classe
		// Par convention (voir ton ConstructorDeclaration), l'étiquette est
		// "Constructor_NomDeLaClasse"
		fragment.add(_factory.createCall("Constructor_" + this.name, fr.n7.stl.tam.ast.Register.SB));

		// À la fin de cette exécution, le constructeur a fait son RETURN (en nettoyant
		// ses paramètres et le 'this' dupliqué).
		// Il ne reste sur la pile que le tout premier pointeur renvoyé par MAlloc.
		// L'allocation de l'objet est terminée !

		return fragment;

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
