package fr.n7.stl.minijava.ast.type.declaration;

/**
 * Les différents droits d'accès possibles en MiniJava.
 * 
 * PACKAGE n'est pas vraiment utilisé avec des mots-clés,
 * c'est l'accès par défaut si on ne met rien.
 */
public enum AccessRight {
	
	PUBLIC, PACKAGE, PROTECTED, PRIVATE;
	
	@Override
	public String toString() {
		switch (this) {
		case PUBLIC: return "public ";
		case PACKAGE: return ""; // Pas de mot-clé
		case PROTECTED: return "protected ";
		case PRIVATE: return "private ";
		default: throw new IllegalArgumentException( "The default case should never be triggered.");		
		}
	}

}
