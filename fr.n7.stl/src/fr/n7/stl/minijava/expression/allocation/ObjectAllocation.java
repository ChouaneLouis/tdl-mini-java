package fr.n7.stl.minijava.expression.allocation;

import java.util.Iterator;
import java.util.List;

import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.expression.assignable.AssignableExpression;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Library;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;

/**
 * Représente l'expression "new NomClasse(arg1, arg2, ...)".
 *
 * La génération de code suit ce protocole précis :
 *   1. LOADL taille  → taille de l'objet en mots
 *   2. SUBR MAlloc   → alloue sur le tas, laisse le pointeur (this) sur la pile
 *   3. Empiler arg1, arg2, ...
 *   4. LOAD -sizeArgs-1[ST]  → dupliquer le pointeur this au sommet de la pile
 *   5. CALL Constructor_NomClasse_N → appel du constructeur
 *
 * Après le RETURN du constructeur, seul le pointeur MAlloc reste sur la pile.
 * C'est la valeur de l'expression "new ...".
 *
 * On vérifie aussi qu'on n'instancie pas une classe abstraite.
 */
public class ObjectAllocation implements AccessibleExpression, AssignableExpression {

    protected String name;

    protected List<AccessibleExpression> arguments;

    protected ClassType classType;

    public ObjectAllocation(String _name, List<AccessibleExpression> _arguments) {
        this.name = _name;
        this.arguments = _arguments;
    }

    @Override
    public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
        boolean ok = true;
        for (AccessibleExpression accessibleExpression : arguments) {
            ok = ok && accessibleExpression.collectAndPartialResolve(_scope);
        }
        return ok;
    }

    /**
     * On résout le nom de la classe et on vérifie qu'elle est concrète.
     * On ne peut pas faire new Shape() si Shape est abstraite.
     */
    @Override
    public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
        boolean ok = true;

        this.classType = new ClassType(this.name);
        ok = ok && this.classType.completeResolve(_scope);

        for (AccessibleExpression accessibleExpression : arguments) {
            ok = ok && accessibleExpression.completeResolve(_scope);
        }

        if (ok && this.classType.getDeclaration() != null && !this.classType.getDeclaration().isConcrete()) {
            Logger.error("Erreur : Impossible d'instancier la classe abstraite " + this.name);
            return false;
        }

        return ok;
    }

    @Override
    public Type getType() {
        return this.classType != null ? this.classType : new ClassType(name);
    }

    /**
     * Génération du code TAM pour "new NomClasse(args)".
     *
     * Protocole complet :
     *   - MAlloc réserve la mémoire sur le tas et laisse l'adresse (pointeur this) sur la pile
     *   - On empile les arguments un à un
     *   - On duplique "this" depuis sous les arguments (LOAD -sizeArgs-1[ST])
     *   - On appelle le constructeur, qui nettoie args + this et rend la main
     *   - Il ne reste que le premier pointeur this retourné par MAlloc → c'est la valeur de new
     */
    @Override
    public Fragment getCode(TAMFactory _factory) {
        Fragment fragment = _factory.createFragment();

        ClassDeclaration classDecl = this.classType.getDeclaration();
        // Taille minimale de 1 : un objet occupe au moins 1 mot en TAM
        int objectSize = Math.max(1, classDecl.getObjectSize());

        // Étape 1 : allouer l'espace sur le tas
        fragment.add(_factory.createLoadL(objectSize));
        fragment.add(Library.MAlloc); // Remplace la taille par le pointeur vers l'objet

        // Étape 2 : empiler les arguments du constructeur
        int sizeArgs = 0;
        for (AccessibleExpression arg : this.arguments) {
            fragment.append(arg.getCode(_factory));
            sizeArgs += arg.getType().length();
        }

        // Étape 3 : dupliquer le pointeur "this" au-dessus des arguments
        // Il est à -sizeArgs-1 depuis le sommet de la pile (ST)
        fragment.add(_factory.createLoad(fr.n7.stl.tam.ast.Register.ST, -sizeArgs - 1, 1));

        // Étape 4 : appel du constructeur (label = Constructor_NomClasse_NbParamsTotal)
        int totalParams = this.arguments.size() + 1; // +1 pour "this"
        fragment.add(_factory.createCall("Constructor_" + this.name + "_" + totalParams, fr.n7.stl.tam.ast.Register.SB));

        // Après le RETURN du constructeur, seul le pointeur MAlloc reste sur la pile

        return fragment;
    }

    @Override
    public String toString() {
        String image = "new " + this.name + "( ";
        Iterator<AccessibleExpression> iterator = this.arguments.iterator();
        if (iterator.hasNext()) {
            AccessibleExpression argument = iterator.next();
            image += argument;
            while (iterator.hasNext()) {
                argument = iterator.next();
                image += " ," + argument;
            }
        }
        image += ")";
        return image;
    }

}
