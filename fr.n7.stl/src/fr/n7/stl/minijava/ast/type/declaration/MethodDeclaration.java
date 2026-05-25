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

public class MethodDeclaration extends ClassElement implements Instruction {

    protected boolean concrete;

    protected List<ParameterDeclaration> parameters;

    protected Block body;

    protected Type type;

    public MethodDeclaration(String _name, Type _type, List<ParameterDeclaration> _parameters, Block _body) {
        super(_name);
        this.parameters = _parameters;
        this.body = _body;
        this.concrete = (_body != null);
        this.type = _type;
    }

    public MethodDeclaration(String _name, Type _type, List<ParameterDeclaration> _parameters) {
        this(_name, _type, _parameters, null);
    }

    @Override
    public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
        boolean isValid = true;
        // La méthode ouvre une nouvelle portée pour ses paramètres et variables locales
        HierarchicalScope<Declaration> methodScope = new SymbolTable(_scope);

        for (ParameterDeclaration parameter : this.parameters) {
            methodScope.register(parameter);
        }

        if (this.concrete) {
            fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration dummy = new fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration(this.name, this.type, this.parameters, this.body);
            isValid = isValid && this.body.collectAndPartialResolve(methodScope, dummy);
        }
        return isValid;
    }

    @Override
    public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
        return this.collectAndPartialResolve(_scope);
    }

    @Override
    public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
        boolean isValid = true;
        // On résout le type de retour
        isValid = isValid && this.type.completeResolve(_scope);

        for (ParameterDeclaration parameter : this.parameters) {
            isValid = isValid && parameter.completeResolve(_scope);
        }

        if (this.concrete) {
            isValid = isValid && this.body.completeResolve(_scope);
        }
        return isValid;
    }

    @Override
    public boolean checkType() {
        boolean isValid = true;
        if (this.concrete) {
            isValid = isValid && this.body.checkType();
            // Remarque : La vérification stricte du fait que le bloc renvoie bien
            // un type compatible avec this.type est généralement gérée
            // par l'instruction 'Return' à l'intérieur du bloc.
        }
        return isValid;
    }

    @Override
    public int allocateMemory(Register _register, int _offset) {
        if (this.concrete) {
            // En TAM, les variables locales d'une fonction commencent à l'offset 3 de LB
            this.body.allocateMemory(Register.LB, 3);
        }
        // Retourne 0 car la définition de la méthode ne prend pas de place mémoire au
        // sein de l'objet lui-même
        return 0;
    }

    @Override
    public Fragment getCode(TAMFactory _factory) {
        Fragment fragment = _factory.createFragment();

        if (this.concrete) {
            // On génère le code du corps de la méthode
            fragment.append(this.body.getCode(_factory));

            // On attache l'étiquette au fragment pour pouvoir appeler cette méthode plus
            // tard
            fragment.addPrefix("Method_" + this.name);
        }

        return fragment;
    }

    @Override
    public String toString() {
        String image = "";
        if (!this.concrete) {
            image += "abstract ";
        }
        image += this.accessRight + " " + this.type + " " + this.name + "( ";
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
        if (this.concrete) {
            image += this.body;
        } else {
            image += ";";
        }
        return image;
    }

    @Override
    public Type getType() {
        return this.type;
    }

    public List<ParameterDeclaration> getParameters() {
        // J'ai ajouté cette méthode pour pouvoir récupérer les paramètres d'une méthode déclarée
        // et vérifier lors d'un appel (MethodCall) si les arguments passés sont du bon type.
        return this.parameters;
    }

}
