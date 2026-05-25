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
import fr.n7.stl.minijava.ast.type.ClassType;
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

    // On sauvegarde la table des symboles de la classe pour y accéder plus tard
    public HierarchicalScope<Declaration> classScope;

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
        if (_scope.accepts(this)) {
            _scope.register(this);

            this.classScope = new SymbolTable(_scope);

            // On enregistre 'this' et 'super' dans la portée de la classe
            fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration thisParam = new fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration(
                    "this", new ClassType(this));
            this.classScope.register(thisParam);

            if (this.ancestor != null) {
                fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration superParam = new fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration(
                        "super", new ClassType(this.ancestor));
                this.classScope.register(superParam);
            }

            boolean b = true;
            for (ClassElement classElement : elements) {
                if (classElement instanceof Declaration) {
                    // Attention : les constructeurs ont le même nom que la classe !
                    // Si on les met dans la table des symboles, ils vont écraser le type de la
                    // classe en cas de recherche !
                    // Donc on les ignore ici.
                    if (!(classElement instanceof fr.n7.stl.minijava.ast.type.declaration.ConstructorDeclaration)) {
                        Declaration declaration = (Declaration) classElement;
                        this.classScope.register(declaration);
                    } else {
                        ConstructorDeclaration cons = (ConstructorDeclaration) classElement;
                        cons.name = "constructor_" + cons.name;
                        this.classScope.register(cons);
                    }
                } else {
                    Logger.error("ClassElement " + classElement.toString() + " is not a Declaration\n");
                    b = false;
                }
            }
            for (ClassElement classElement : elements) {
                if (classElement instanceof Instruction) {
                    b = b && ((Instruction) classElement).collectAndPartialResolve(this.classScope);
                }
            }
            return b;
        } else {
            Logger.error("class " + this.name + " is already defined.");
            return false;
        }
    }

    @Override
    public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
        // Le conteneur de fonction n'a pas de sens directement pour une classe globale
        return this.collectAndPartialResolve(_scope);
    }

    @Override
    public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
        boolean result = true;

        // Vérification de la classe mère si elle existe
        if (this.ancestor != null) {
            Declaration ancestorDeclaration = _scope.get(this.ancestor);
            if (ancestorDeclaration == null) {
                Logger.error("Class " + this.name + " extends an unknown class " + this.ancestor);
                return false;
            }
            if (!(ancestorDeclaration instanceof ClassDeclaration)) {
                Logger.error("Class " + this.name + " cannot extend " + this.ancestor + " because it is not a class.");
                return false;
            }
        }

        // On résout les éléments internes à la classe avec la table des symboles de la
        // classe
        for (ClassElement element : elements) {
            if (element instanceof Instruction) {
                result = result && ((Instruction) element).completeResolve(this.classScope);
            } else if (element instanceof fr.n7.stl.minijava.ast.type.declaration.AttributeDeclaration) {
                // Il faut bien penser à appeler completeResolve sur le type de l'attribut,
                // sinon ça plante car le type reste null après
                result = result && ((fr.n7.stl.minijava.ast.type.declaration.AttributeDeclaration) element).getType()
                        .completeResolve(this.classScope);
            }
        }

        return result;
    }

    @Override
    public boolean checkType() {
        boolean result = true;
        // On vérifie le bon typage de chaque attribut, méthode ou constructeur
        for (ClassElement element : elements) {
            if (element instanceof Instruction) {
                result = result && ((Instruction) element).checkType();
            }
        }
        return result;
    }

    @Override
    public int allocateMemory(Register _register, int _offset) {
        // La déclaration de la classe en soi ne prend pas de place sur la pile
        // principale.
        // Cependant, on peut déclencher l'allocation mémoire pour ses éléments
        // statiques
        // ou calculer les offsets des attributs d'instance.
        for (ClassElement element : elements) {
            if (element instanceof Instruction) {
                // On laisse chaque élément gérer son allocation interne si nécessaire
                ((Instruction) element).allocateMemory(_register, _offset);
            }
        }
        return _offset; // Retourne 0 car la taille de la classe n'impacte pas l'offset du bloc courant
    }

    @Override
    public Fragment getCode(TAMFactory _factory) {
        Fragment fragment = _factory.createFragment();
        // On récupère le code de chaque élément (notamment les corps des méthodes)
        for (ClassElement element : elements) {
            if (element instanceof Instruction) {
                fragment.append(((Instruction) element).getCode(_factory));
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
        return new ClassType(this);
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

    public List<ClassElement> getElements() {
        return elements;
    }

    public String getAncestor() {
        return ancestor;
    }

}
