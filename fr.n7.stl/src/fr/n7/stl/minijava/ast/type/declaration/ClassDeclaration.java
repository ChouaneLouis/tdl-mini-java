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

    public List<ClassElement> getElements() {
        return elements;
    }

    protected boolean concrete;

    public boolean isConcrete() {
        return this.concrete;
    }

    protected String name;

    protected String ancestor;
    protected ClassDeclaration ancestorDecl;

    public String getAncestor() {
        return this.ancestor;
    }
    
    public ClassDeclaration getAncestorDecl() {
        return this.ancestorDecl;
    }

    private SymbolTable classScope;

    /**
     * 
     */
    public ClassDeclaration(boolean _concrete, String _name, String _ancestor, List<ClassElement> _elements) {
        this.concrete = _concrete;
        this.name = _name;
        this.ancestor = _ancestor;
        this.elements = _elements;
        boolean hasConstructor = false;
        for (ClassElement el : this.elements) {
            if (el instanceof ConstructorDeclaration) {
                hasConstructor = true;
            }
            if (el instanceof MethodDeclaration) {
                ((MethodDeclaration) el).setClassName(this.name);
            }
        }
        if (!hasConstructor && !this.name.equals("Main")) { // Main doesn't need it usually, but it won't hurt. Still, let's keep it clean
            ConstructorDeclaration defaultCons = new ConstructorDeclaration(this.name, new java.util.LinkedList<>(), new fr.n7.stl.minic.ast.Block(new java.util.LinkedList<>()));
            this.elements.add(defaultCons);
        }
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
        // Inject $currentClass in scope to know the current class during encapsulation checks
        this.classScope.register(new fr.n7.stl.minic.ast.instruction.declaration.VariableDeclaration("$currentClass", getType(), null));

        // La déclaration de 'this' ne doit PAS être mise dans le scope de la classe, 
        // car elle n'existe pas pour les méthodes statiques. Elle sera injectée uniquement 
        // dans les paramètres des méthodes non-statiques plus bas.

        boolean isValid = true;

        for (ClassElement classElement : this.elements) {
            // System.out.println(classElement.getClass().toString());

            if (classElement instanceof ConstructorDeclaration) {
                ConstructorDeclaration cd = (ConstructorDeclaration) classElement;
                // This is passed LAST for constructors
                cd.parameters.add(new ParameterDeclaration("this", getType()));

                isValid = isValid && cd.collectAndPartialResolve(this.classScope);

            } else if (classElement instanceof AttributeDeclaration) {
                AttributeDeclaration ad = (AttributeDeclaration) classElement;
                if (!this.classScope.accepts(ad)) {
                    Logger.error(ad.name + " déjà défini");
                    return false;
                }
                this.classScope.register(ad);

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
                    md.parameters.add(0, new ParameterDeclaration("this", getType()));
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
            } else {
                Declaration decl = _scope.get(this.ancestor);
                if (decl instanceof ClassDeclaration) {
                    this.ancestorDecl = (ClassDeclaration) decl;
                } else {
                    Logger.error("L'ancêtre '" + this.ancestor + "' n'est pas une classe.");
                    isValid = false;
                }
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
        /// EDITED calcul des offsets
        int localOffset = (this.ancestorDecl != null) ? this.ancestorDecl.getObjectSize() : 0;
        for (ClassElement classElement : elements) {
            if (classElement instanceof AttributeDeclaration) {
                AttributeDeclaration atr = (AttributeDeclaration) classElement;
                atr.offset = localOffset;
                localOffset += atr.getType().length();
            } else if (classElement instanceof MethodDeclaration) {
                MethodDeclaration md = (MethodDeclaration) classElement;
                if (md.isConcrete()) {
                    md.getFunction().allocateMemory(Register.LB, 3);
                }
            } else if (classElement instanceof ConstructorDeclaration) {
                ConstructorDeclaration cd = (ConstructorDeclaration) classElement;
                cd.allocateMemory(Register.LB, 3);
            }
        }
        return _offset; // Ne prend pas de place en mémoire
    }

    @Override
    public Fragment getCode(TAMFactory _factory) {
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
                    methode.addPrefix("Method_" + this.name + "_" + md.getName());
                    int paramSize = 0;
                    for (fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration p : md.getParameters()) {
                        paramSize += p.getType().length();
                    }
                    methode.add(_factory.createReturn(0, paramSize));
                    fragment.append(methode);
                }
            } else {
                Logger.error(classElement.name + " n'est pas du bon type");
            }
        }
        fragment.addSuffix(skipLabel);

        return fragment;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Type getType() {
        /// EDITED
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

    public int getObjectSize() {
        int size = (this.ancestorDecl != null) ? this.ancestorDecl.getObjectSize() : 0;
        for (ClassElement element : this.elements) {
            if (element instanceof AttributeDeclaration) {
                size += ((AttributeDeclaration) element).getType().length();
            }
        }
        return size;
    }

}
