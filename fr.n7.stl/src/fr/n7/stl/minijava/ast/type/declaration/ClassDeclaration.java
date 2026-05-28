/**
 * 
 */
package fr.n7.stl.minijava.ast.type.declaration;

import java.util.List;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.instruction.Instruction;
import fr.n7.stl.minic.ast.instruction.Return;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.scope.SymbolTable;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.tam.ast.TAMInstruction;
import fr.n7.stl.util.Logger;

/**
 * 
 */
public class ClassDeclaration implements Instruction, Declaration {

    protected List<ClassElement> elements;

<<<<<<< HEAD
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
            fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration thisParam = 
                new fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration("this", new ClassType(this));
            this.classScope.register(thisParam);
            
            if (this.ancestor != null) {
                fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration superParam = 
                    new fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration("super", new ClassType(this.ancestor));
                this.classScope.register(superParam);
            }


            boolean b = true;
            for (ClassElement classElement : elements) {
                if (classElement instanceof Declaration) {
                    // Attention : les constructeurs ont le même nom que la classe !
                    // Si on les met dans la table des symboles, ils vont écraser le type de la classe en cas de recherche !
                    // Donc on les ignore ici.
                    if (!(classElement instanceof ConstructorDeclaration)) {
                        Declaration declaration = (Declaration) classElement;
                        if (this.classScope.accepts(declaration)) { 
                            this.classScope.register(declaration);
                        } else {
                            Logger.error("Declaration of " + declaration.getName() + " already exists in class " + this.name);
                        } 
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
                // Il faut bien penser à appeler completeResolve sur le type de l'attribut, sinon ça plante car le type reste null après
                result = result && ((fr.n7.stl.minijava.ast.type.declaration.AttributeDeclaration) element).getType().completeResolve(this.classScope);
            }
        }

        return result;
=======
    public List<ClassElement> getElements() {
        return elements;
    }

    protected boolean concrete;

    public boolean isConcrete() {
        return this.concrete;
    }

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

        this.classScope = new SymbolTable(_scope);

        // La déclaration de 'this' ne doit PAS être mise dans le scope de la classe, 
        // car elle n'existe pas pour les méthodes statiques. Elle sera injectée uniquement 
        // dans les paramètres des méthodes non-statiques plus bas.
        ParameterDeclaration thisDeclaration = new ParameterDeclaration("this", getType());

        boolean isValid = true;

        for (ClassElement classElement : this.elements) {
            // System.out.println(classElement.getClass().toString());

            if (classElement instanceof ConstructorDeclaration) {
                ConstructorDeclaration cd = (ConstructorDeclaration) classElement;
                // This
                cd.parameters.add(thisDeclaration);

                isValid = isValid && cd.collectAndPartialResolve(this.classScope);

            } else if (classElement instanceof AttributeDeclaration) {
                AttributeDeclaration ad = (AttributeDeclaration) classElement;
                if (!_scope.accepts(ad)) {
                    Logger.error(ad.name + " déjà défini");
                    return false;
                }
                _scope.register(ad);

            } else if (classElement instanceof MethodDeclaration) {
                MethodDeclaration md = (MethodDeclaration) classElement;

                // Ajout au scope global si public
                if (md.getAccessRight() == AccessRight.PUBLIC) {
                    if (_scope.accepts(md)) {
                        _scope.register(md);
                    }
                    // Si la méthode existe déjà (ex: héritage ou même nom dans une autre classe), on l'ignore silencieusement car la résolution par scope global est très basique dans ce compilateur.
                }

                // This
                if (md.getElementKind() != ElementKind.CLASS) {
                    md.parameters.add(0, thisDeclaration);
                }

                isValid = isValid && md.collectAndPartialResolve(this.classScope);
            } else {
                Logger.error(classElement.name + " n'est pas du bon type");
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
        // ClassDeclaration.")

        boolean isValid = true;

        if (this.ancestor != null) {
            if (!_scope.knows(this.ancestor)) {
                Logger.error("La classe mère '" + this.ancestor + "' de " + this.name + " n'existe pas.");
                isValid = false;
            }
        }

        for (ClassElement classElement : this.elements) {

            if (classElement instanceof ConstructorDeclaration) {
                ConstructorDeclaration cd = (ConstructorDeclaration) classElement;
                isValid = isValid && cd.completeResolve(this.classScope);

            } else if (classElement instanceof AttributeDeclaration) {
                AttributeDeclaration attribute = (AttributeDeclaration) classElement;
                isValid = isValid && attribute.getType().completeResolve(this.classScope);

            } else if (classElement instanceof MethodDeclaration) {
                MethodDeclaration md = (MethodDeclaration) classElement;
                isValid = isValid && md.completeResolve(this.classScope);

            } else {
                Logger.error(classElement.name + " n'est pas du bon type");
            }
        }

        return isValid;
>>>>>>> alexis_temp
    }

    @Override
    public boolean checkType() {
<<<<<<< HEAD
        boolean result = true;
        // On vérifie le bon typage de chaque attribut, méthode ou constructeur
        for (ClassElement element : elements) {
            if (element instanceof Instruction) {
                result = result && ((Instruction) element).checkType();
            }
        }
        return result;
=======
        // throw new SemanticsUndefinedException("Semantics check type is undefined in
        // ClassDeclaration.");
        boolean ok = true;
        for (ClassElement classElement : elements) {
            if (classElement instanceof Instruction)
                ok = ok && ((Instruction) classElement).checkType();
        }
        return ok;
>>>>>>> alexis_temp
    }

    @Override
    public int allocateMemory(Register _register, int _offset) {
<<<<<<< HEAD
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
        return 0; // Retourne 0 car la taille de la classe n'impacte pas l'offset du bloc courant
=======
        /// EDITED calcul des offsets
        int localOffset = 0;
        for (ClassElement classElement : elements) {
            if (classElement instanceof AttributeDeclaration) {
                AttributeDeclaration atr = (AttributeDeclaration) classElement;
                atr.offset = localOffset;
                localOffset += atr.getType().length();
            } else if (classElement instanceof MethodDeclaration) {
                MethodDeclaration md = (MethodDeclaration) classElement;
                md.getFunction().allocateMemory(Register.LB, 3);
            } else if (classElement instanceof ConstructorDeclaration) {
                ConstructorDeclaration cd = (ConstructorDeclaration) classElement;
                cd.allocateMemory(Register.LB, 3);
            }
        }
        return _offset; // Ne prend pas de place en mémoire
>>>>>>> alexis_temp
    }

    @Override
    public Fragment getCode(TAMFactory _factory) {
<<<<<<< HEAD
        Fragment fragment = _factory.createFragment();
        // On récupère le code de chaque élément (notamment les corps des méthodes)
        for (ClassElement element : elements) {
            if (element instanceof Instruction) {
                fragment.append(((Instruction) element).getCode(_factory));
            }
        }
=======
        // throw new SemanticsUndefinedException("Semantics get code is undefined in
        // ClassDeclaration.");
        Fragment fragment = _factory.createFragment();
        String skipLabel = "skip_" + this.name;
        fragment.add(_factory.createJump(skipLabel));

        // On parcourt tous les éléments définis dans la classe
        for (ClassElement classElement : this.elements) {

            // Seules les méthodes et les constructeurs
            // ont du code exécutable à générer. Les attributs sont ignorés ici.
            // System.out.println(classElement.getClass().toString());

            if (classElement instanceof ConstructorDeclaration) {
                ConstructorDeclaration cd = (ConstructorDeclaration) classElement;

                fragment.append(cd.getCode(_factory));

            } else if (classElement instanceof AttributeDeclaration) {
                AttributeDeclaration attribute = (AttributeDeclaration) classElement;
                // TODO : attribute getCode dans class declaration ?

            } else if (classElement instanceof MethodDeclaration) {
                MethodDeclaration md = (MethodDeclaration) classElement;
                if (md.isConcrete()) {
                    Fragment methode = md.body.getCode(_factory);
                    methode.addPrefix(md.getName());
                    fragment.append(methode);
                }
            } else {
                Logger.error(classElement.name + " n'est pas du bon type");
            }
        }
        fragment.addSuffix(skipLabel);

>>>>>>> alexis_temp
        return fragment;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Type getType() {
<<<<<<< HEAD
=======
        /// EDITED
>>>>>>> alexis_temp
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

<<<<<<< HEAD
    public List<ClassElement> getElements() {
        return elements;
    }

    public String getAncestor() {
        return ancestor;
=======
    public int getObjectSize() {
        int size = 0;
        for (ClassElement element : this.elements) {
            if (element instanceof AttributeDeclaration) {
                size += ((AttributeDeclaration) element).getType().length();
            }
        }
        return size;
>>>>>>> alexis_temp
    }

}
