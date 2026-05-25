package fr.n7.stl.minijava.ast.type.declaration;

import java.util.Iterator;
import java.util.List;

import fr.n7.stl.minic.ast.Block;
import fr.n7.stl.minic.ast.instruction.Instruction;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.scope.SymbolTable;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;

public class ConstructorDeclaration extends ClassElement implements Instruction {

    protected List<ParameterDeclaration> parameters;

    protected Block body;

    public ConstructorDeclaration(String _name, List<ParameterDeclaration> _parameters, Block _body) {
        super(_name);
        this.parameters = _parameters;
        this.body = _body;
    }

    @Override
    public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
        // Le constructeur ouvre une nouvelle portée pour ses paramètres et variables
        // locales
        HierarchicalScope<Declaration> constructorScope = new SymbolTable(_scope);
        boolean isValid = true;

        for (ParameterDeclaration parameter : this.parameters) {
            constructorScope.register(parameter);
        }

        isValid = isValid && this.body.collectAndPartialResolve(constructorScope);
        return isValid;
    }

    @Override
    public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
        boolean isValid = true;
        for (ParameterDeclaration parameter : this.parameters) {
            isValid = isValid && parameter.completeResolve(_scope);
        }
        isValid = isValid && this.body.completeResolve(_scope);
        return isValid;
    }

    @Override
    public boolean checkType() {
        // Un constructeur n'a pas de valeur de retour à vérifier, on vérifie juste son
        // corps.
        return this.body.checkType();
    }

    @Override
    public int allocateMemory(Register _register, int _offset) {
        // En assembleur TAM, les variables locales d'une fonction/constructeur
        // commencent généralement à l'offset 3 du registre LB (Local Base).
        // (Les offset < 0 ou spécifiques sont gérés lors de l'appel).
        this.body.allocateMemory(Register.LB, 3);

        // Retourne 0 car un constructeur ne prend pas de place au niveau de la
        // déclaration de la classe
        return 0;
    }

    @Override
    public Fragment getCode(TAMFactory _factory) {
        Fragment fragment = _factory.createFragment();

        // On génère d'abord le code du corps du constructeur
        fragment.append(this.body.getCode(_factory));

        // Calcul de la taille des paramètres à dépiler lors du RETURN
        int parametersSize = 0;
        for (ParameterDeclaration parameter : this.parameters) {
            parametersSize += parameter.getType().length();
        }

        // On ajoute l'instruction de retour à la fin
        fragment.add(_factory.createReturn(0, parametersSize));

        fragment.addPrefix("Constructor_" + this.name);

        return fragment;
    }

    @Override
    public Type getType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public List<ParameterDeclaration> getParameters() {
        return this.parameters;
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
    public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
        return this.collectAndPartialResolve(_scope);
    }
}
