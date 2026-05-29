package fr.n7.stl.minijava.ast.type.declaration;

import java.util.Iterator;
import java.util.List;

import fr.n7.stl.minic.ast.Block;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.scope.SymbolTable;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;

/**
 * Déclaration d'un constructeur dans une classe MiniJava.
 *
 * Un constructeur n'a pas de type de retour et porte le même nom que sa classe.
 * Le paramètre implicite "this" est injecté par ClassDeclaration AVANT d'appeler
 * collectAndPartialResolve ici (il est donc déjà dans la liste des params à ce moment).
 *
 * Particularité : on injecte un marqueur $isConstructor dans le scope du constructeur
 * pour que les instructions super() et this() puissent vérifier qu'elles sont bien
 * utilisées dans un contexte de constructeur (et lever une erreur sinon).
 */
public class ConstructorDeclaration extends ClassElement {

    protected List<ParameterDeclaration> parameters;

    public List<ParameterDeclaration> getParameters() {
        return parameters;
    }

    protected Block body;

    public ConstructorDeclaration(String _name, List<ParameterDeclaration> _parameters, Block _body) {
        super(_name);
        this.parameters = _parameters;
        this.body = _body;
    }

    @Override
    public String toString() {
        String image = "";
        image += this.accessRight + " " + this.name + "( ";
        Iterator<ParameterDeclaration> iterator = this.parameters.iterator();
        if (iterator.hasNext()) {
            ParameterDeclaration parameter = iterator.next();
            image += parameter;
            while (iterator.hasNext()) {
                parameter = iterator.next();
                image += " ," + parameter;
            }
        }
        image += ")";
        image += this.body;
        return image;
    }

    @Override
    public Type getType() {
        // Un constructeur n'a pas de type de retour
        throw new fr.n7.stl.minic.ast.SemanticsUndefinedException("Semantics get type is undefined in ConstructorDeclaration.");
    }

    protected HierarchicalScope<Declaration> consScope;

    /**
     * Phase 1 : on crée un scope local pour le constructeur.
     *
     * On y injecte le marqueur $isConstructor pour que super() et this()
     * sachent qu'ils sont dans un constructeur. Sans ça, une erreur est levée.
     * Ensuite on enregistre tous les paramètres formels dans ce scope.
     */
    public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
        this.consScope = new SymbolTable(_scope);
        // Marqueur de contexte : permet à super() et this() de vérifier leur contexte d'appel
        this.consScope.register(new ContextMarker("$isConstructor"));
        for (ParameterDeclaration parameterDeclaration : parameters) {
            consScope.register(parameterDeclaration);
        }
        return this.body.collectAndPartialResolve(consScope);
    }

    public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
        return this.body.completeResolve(consScope);
    }

    /**
     * Calcul des offsets des paramètres dans le cadre de pile.
     *
     * Les paramètres sont sous LB dans la pile (offsets négatifs).
     * On commence par le paramètre le plus loin (le plus à gauche dans la signature)
     * et on remonte vers -1[LB] qui est "this" (injecté en dernier par ClassDeclaration).
     */
    public int allocateMemory(fr.n7.stl.tam.ast.Register _register, int _offset) {
        int totalParamSize = 0;
        for (ParameterDeclaration param : this.parameters) {
            totalParamSize += param.getType().length();
        }
        int paramOffset = -totalParamSize;
        for (fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration param : this.parameters) {
            param.setOffset(paramOffset);
            paramOffset += param.getType().length();
        }
        // Les variables locales du constructeur commencent à 3[LB] (convention TAM)
        this.body.allocateMemory(fr.n7.stl.tam.ast.Register.LB, 3);
        return _offset;
    }

    /**
     * Génération du code TAM du constructeur.
     *
     * Le label est de la forme Constructor_NomClasse_NbParams (ex: Constructor_Point_3).
     * À la fin, RETURN (0) sizeOfParams dépile les paramètres et "this" sans rien retourner.
     */
    public Fragment getCode(TAMFactory _factory) {
        Fragment cons = body.getCode(_factory);
        cons.addPrefix("Constructor_" + this.name + "_" + parameters.size());
        int sizeOfParams = 0;
        for (ParameterDeclaration parameterDeclaration : parameters) {
            sizeOfParams += parameterDeclaration.getType().length();
        }
        cons.add(_factory.createReturn(0, sizeOfParams));
        return cons;
    }
}
