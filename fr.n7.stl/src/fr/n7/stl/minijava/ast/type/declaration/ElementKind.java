package fr.n7.stl.minijava.ast.type.declaration;

/**
 * Pour savoir si un élément appartient à l'instance ou à la classe.
 * 
 * OBJECT = lié à l'instance (méthodes normales, attributs)
 * CLASS = statique (mot-clé static)
 */
public enum ElementKind {
	
	OBJECT,
	CLASS;
	
	@Override
	public String toString() {
		switch (this) {
		case OBJECT : return ""; // Pas de mot-clé
		case CLASS : return "static ";
		default: throw new IllegalArgumentException( "The default case should never be triggered.");
		}
	}

}
