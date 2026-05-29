package fr.n7.stl.minijava.ast.type.declaration;

import java.util.Iterator;
import java.util.List;

import fr.n7.stl.minic.ast.Block;
import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.scope.SymbolTable;
import fr.n7.stl.minic.ast.type.Type;

public class MethodDeclaration extends ClassElement {

    protected boolean concrete;

    public boolean isConcrete() {
        return this.concrete;
    }

    protected List<ParameterDeclaration> parameters;
    
    public List<ParameterDeclaration> getParameters() {
        return parameters;
    }

    protected Block body;

    protected Type type;

    protected FunctionDeclaration function;

    public FunctionDeclaration getFunction() {
        return function;
    }

    public MethodDeclaration(String _name, Type _type, List<ParameterDeclaration> _parameters, Block _body) {
        super(_name);
        this.parameters = _parameters;
        this.body = _body;
        this.concrete = (_body != null);
        this.type = _type;

        this.function = new FunctionDeclaration(this.name, this.type, this.parameters, this.body);
    }

    public void setClassName(String _className) {
        this.function.setName("Method_" + _className + "_" + this.name);
    }

    public MethodDeclaration(String _name, Type _type, List<ParameterDeclaration> _parameters) {
        this(_name, _type, _parameters, null);
    }

    @Override
    public String toString() {
        String image = "";
        if (!this.concrete) {
            image += "abstract ";
        }
        image += this.accessRight + " " + this.elementKind + this.type + " " + this.name + "( ";
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
        /// EDITED
        return this.type;

    }

    protected HierarchicalScope<Declaration> consScope;

    public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
        this.consScope = new SymbolTable(_scope);
        for (ParameterDeclaration parameterDeclaration : parameters) {
            consScope.register(parameterDeclaration);
        }
        if (this.concrete) {
            return this.body.collectAndPartialResolve(consScope, this.function);
        }
        return true;
    }

    public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
        if (this.concrete) {
            return this.body.completeResolve(consScope);
        }
        return true;
    }

}
