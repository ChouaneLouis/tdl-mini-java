package fr.n7.stl.minijava.ast.type.declaration;

import fr.n7.stl.minic.ast.scope.Declaration;

/**
 * Classe mère pour tout ce qui peut se trouver dans une classe :
 * attributs, méthodes, constructeurs.
 * 
 * Ça permet de gérer pour tout le monde les mots-clés comme
 * static (ElementKind) et public/private (AccessRight).
 */
public abstract class ClassElement implements Declaration {
	
	// Est-ce que c'est lié à l'instance (OBJECT) ou statique (CLASS) ?
	protected ElementKind elementKind;
	
	// public, private, protected ou package
	protected AccessRight accessRight;
	
	protected String name;
	
	public ClassElement(ElementKind _elementKind, AccessRight _accessRight, String _name) {
		this.elementKind = _elementKind;
		this.accessRight = _accessRight;
		this.name = _name;
	}
	
	/**
	 * Par défaut, un élément est une méthode/attribut d'instance (OBJECT)
	 * avec une visibilité package.
	 */
	public ClassElement(String _name) {
		this( ElementKind.OBJECT, AccessRight.PACKAGE, _name);
	}
	
	public ElementKind getElementKind() {
		return this.elementKind;
	}
	
	public void setElementKind(ElementKind _elementKind) {
		this.elementKind = _elementKind;
	}
	
	public AccessRight getAccessRight() {
		return this.accessRight;
	}
	
	public void setAccessRight(AccessRight _accessRight) {
		this.accessRight = _accessRight;
	}
	
	@Override
	public String getName() {
		return this.name;
	}

}
