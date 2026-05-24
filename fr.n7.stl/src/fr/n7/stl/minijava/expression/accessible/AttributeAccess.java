package fr.n7.stl.minijava.expression.accessible;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.minijava.ast.type.declaration.AttributeDeclaration;
import fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration;
import fr.n7.stl.minijava.ast.type.declaration.ClassElement;
import fr.n7.stl.minijava.expression.AbstractAttribute;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Library;
import fr.n7.stl.tam.ast.TAMFactory;

public class AttributeAccess extends AbstractAttribute<AccessibleExpression> implements AccessibleExpression {

	public AttributeAccess(AccessibleExpression _object, String _name) {
		super(_object, _name);
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		// on vérifie l'objet caar l'attribut en dépend
		// ex Coord.x 
		// il faut chech si Coord est bien
		return this.object.collectAndPartialResolve(_scope);

	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		//on vérifie que l'objet est ok 
		boolean ok = this.object.collectAndPartialResolve(_scope);
		if (!ok){return false;}

		//type de l'objet
		Type objectType = this.object.getType();

		//On chech si ce type est bien une classeType 
		// que c'est bien Coord par exemple 
		if (objectType instanceof ClassType){
			//on récupère la déclaration de notre objet
			String className = ((ClassType) objectType).getName();
			Declaration classDecl = _scope.get(className);

			if (classDecl instanceof ClassDeclaration){

				ClassDeclaration laClass = (ClassDeclaration) classDecl;
				for (ClassElement element : laClass.getElements()) {
					//On veut que les attribut 
					if (element instanceof AttributeDeclaration){

						AttributeDeclaration attr = (AttributeDeclaration) element;

						//on vérifie si notre attribute est bien dans notre objet
						if (attr.getName().equals(this.name)){
							//on enregistre dans AbstractAttribute l'attribute
							this.attribute= attr;
							return true;
						}
					}
				}

			}

		
	

		}
		System.err.println("Erreur : " + this.name + "n'existe pas ou " + this.object + " a un pb");
		return false;
	}

	@Override
	public Type getType() {
		//le type est celui de l'objet
		if (this.attribute != null) {
            return this.attribute.getType();
        }
        return null;

	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment result = _factory.createFragment();
        
        // 1. On charge l'adresse de l'objet (la référence) au sommet de la pile
        result.append(this.object.getCode(_factory));
        
        // 2. On ajoute l'offset (le décalage) de l'attribut dans la classe
        result.add(_factory.createLoadL(this.attribute.getOffset()));
        result.add(Library.IAdd); 
        // L'adresse exacte de l'attribut = (Adresse de l'objet) + (Décalage de l'attribut)
        
        // 3. On lit la mémoire à cette adresse (Load Indirect) de la taille de l'attribut
        result.add(_factory.createLoadI(this.getType().length()));
        
        return result;

	}

}
