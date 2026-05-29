package fr.n7.stl.minijava.expression.accessible;

import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.expression.accessible.BinaryOperator;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minijava.expression.AbstractAttribute;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;

/**
 * Lecture d'un attribut d'objet en rvalue (côté droit d'une expression).
 *
 * Ex : a.v, this.x, super.color
 *
 * Génération de code :
 *   1. Évaluer l'objet → empile le pointeur vers l'objet sur la pile
 *   2. LOADL offset → offset de l'attribut dans la structure de l'objet
 *   3. SUBR IAdd    → calcule l'adresse de l'attribut
 *   4. LOADI (size) → charge la valeur à cette adresse
 */
public class AttributeAccess extends AbstractAttribute<AccessibleExpression> implements AccessibleExpression {

    public AttributeAccess(AccessibleExpression _object, String _name) {
        super(_object, _name);
    }

    @Override
    public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
        return this.object.collectAndPartialResolve(_scope);
    }

    @Override
    public Fragment getCode(TAMFactory _factory) {
        Fragment f = _factory.createFragment();
        // Empile le pointeur vers l'objet
        f.append(this.object.getCode(_factory));
        // Calcule l'adresse de l'attribut = pointeur + offset
        f.add(_factory.createLoadL(this.attribute.getOffset()));
        f.add(TAMFactory.createBinaryOperator(BinaryOperator.Add));
        // Charge la valeur pointée
        f.add(_factory.createLoadI(this.attribute.getType().length()));
        return f;
    }

}
