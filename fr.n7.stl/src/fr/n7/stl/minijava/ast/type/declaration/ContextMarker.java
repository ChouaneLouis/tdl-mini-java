package fr.n7.stl.minijava.ast.type.declaration;

import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.type.AtomicType;
import fr.n7.stl.minic.ast.type.Type;

/**
 * Sentinelle de contexte utilisée pour marquer l'environnement de compilation courant.
 * Par exemple, on l'injecte dans le scope d'un constructeur sous le nom "$isConstructor"
 * pour permettre aux instructions super() et this() de détecter qu'elles sont bien
 * à l'intérieur d'un constructeur, et de lever une erreur sinon.
 */
public class ContextMarker implements Declaration {

    private final String name;

    public ContextMarker(String _name) {
        this.name = _name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Type getType() {
        // Pas de type réel, c'est juste une sentinelle
        return AtomicType.BooleanType;
    }
}
