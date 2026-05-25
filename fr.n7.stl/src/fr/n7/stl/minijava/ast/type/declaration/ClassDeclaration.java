/**
 * 
 */
package fr.n7.stl.minijava.ast.type.declaration;

import java.util.List;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.instruction.Instruction;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.scope.SymbolTable;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;

/**
 * 
 */
public class ClassDeclaration implements Instruction, Declaration {

    protected List<ClassElement> elements;

    protected boolean concrete;

    protected String name;

    protected String ancestor;

    private SymbolTable classScope;

    /**
     * 
     */
    public ClassDeclaration(boolean _concrete, String _name, String _ancestor, List<ClassElement> _elements) {
        this.concrete = _concrete;
        this.name = _name;
        this.ancestor = _ancestor;
        this.elements = _elements;
    }

    /**
     * 
     */
    public ClassDeclaration(boolean _concrete, String _name, List<ClassElement> _elements) {
        this(_concrete, _name, null, _elements);
    }

    @Override
    public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
        // throw new SemanticsUndefinedException("Semantics collect is undefined in
        // ClassDeclaration.");
        if (!_scope.accepts(this)) {
            Logger.error("Erreur de collecte : La classe " + this.name + " est déjà définie.");
            return false;
        }

        _scope.register(this);

        this.classScope = new fr.n7.stl.minic.ast.scope.SymbolTable(_scope);

        boolean isValid = true;

        for (ClassElement classElement : this.elements) {

            if (this.classScope.accepts(classElement)) {
                this.classScope.register(classElement);

                if (classElement instanceof Instruction) {
                    isValid = isValid && ((Instruction) classElement).collectAndPartialResolve(this.classScope);
                }
            } else {
                Logger.error(
                        "Double définition de l'élément " + classElement.getName() + " dans la classe " + this.name);
                isValid = false;
            }
        }

        return isValid;
    }

    @Override
    public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
        return this.collectAndPartialResolve(_scope);
    }

    @Override
    public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
        // throw new SemanticsUndefinedException("Semantics resolve is undefined in
        // ClassDeclaration.");
        boolean isValid = true;

        if (this.ancestor != null) {
            if (!_scope.knows(this.ancestor)) {
                Logger.error("La classe mère '" + this.ancestor + "' de " + this.name + " n'existe pas.");
                isValid = false;
            }
        }

        for (ClassElement classElement : this.elements) {

            if (classElement instanceof AttributeDeclaration) {
                AttributeDeclaration attribute = (AttributeDeclaration) classElement;
                isValid = isValid && attribute.getType().completeResolve(this.classScope);
            } else if (classElement instanceof Instruction) {
                isValid = isValid && ((Instruction) classElement).completeResolve(this.classScope);
            }
        }

        return isValid;
    }

    @Override
    public boolean checkType() {
        // throw new SemanticsUndefinedException("Semantics check type is undefined in
        // ClassDeclaration.");
        boolean ok = true;
        for (ClassElement classElement : elements) {
            if (classElement instanceof Instruction)
                ok = ok && ((Instruction) classElement).checkType();
        }
        return ok;
    }

    @Override
    public int allocateMemory(Register _register, int _offset) {
        // throw new SemanticsUndefinedException("Semantics allocation memory is
        // undefined in ClassDeclaration.");
        return _offset; // Ne prend pas de place en mémoire
    }

    @Override
    public Fragment getCode(TAMFactory _factory) {
        // throw new SemanticsUndefinedException("Semantics get code is undefined in
        // ClassDeclaration.");
        Fragment fragment = _factory.createFragment();

        // On parcourt tous les éléments définis dans la classe
        for (ClassElement classElement : this.elements) {

            // Seules les méthodes et les constructeurs (qui implémentent Instruction)
            // ont du code exécutable à générer. Les attributs sont ignorés ici.
            if (classElement instanceof Instruction) {
                Instruction executableElement = (Instruction) classElement;

                // On récupère le fragment de code de la méthode/constructeur
                // et on l'ajoute au fragment global de la classe
                fragment.append(executableElement.getCode(_factory));
            }
        }

        return fragment;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Type getType() {
        // TODO Auto-generated method stub
        throw new SemanticsUndefinedException("Semantics get type is undefined in ClassDeclaration.");
        // return null;
    }

    @Override
    public String toString() {
        String image = "";
        if (!this.concrete) {
            image += "abstract ";
        }
        image += "class " + this.name + " ";
        if (this.ancestor != null) {
            image += "extends " + this.ancestor + " ";
        }
        image += "{\n";
        for (ClassElement e : this.elements) {
            image += e;
        }
        image += "}\n";
        return image;
    }

    public int getObjectSize() {
        int size = 0;
        for (ClassElement element : this.elements) {
            if (element instanceof AttributeDeclaration) {
                size += ((AttributeDeclaration) element).getType().length();
            }
        }
        return size;
    }

}
