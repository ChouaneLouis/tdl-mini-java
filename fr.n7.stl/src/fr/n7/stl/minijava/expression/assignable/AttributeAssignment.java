package fr.n7.stl.minijava.expression.assignable;

import fr.n7.stl.minic.ast.expression.assignable.AssignableExpression;
import fr.n7.stl.minijava.expression.AbstractAttribute;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;

/**
 * Écriture dans un attribut d'objet en lvalue (côté gauche d'une affectation).
 *
 * Ex : this.x = ..., a.v = ..., super.id = ...
 *
 * En MiniJava, les variables de type classe sont des POINTEURS vers l'objet sur le tas.
 * Donc quand on fait "a.v = ...", l'objet 'a' contient une adresse.
 * Il faut d'abord lire cette adresse (LOADI 1), puis calculer l'adresse de l'attribut.
 *
 * Génération de code (retourne l'adresse destination pour que STORE puisse écrire) :
 *   1. Évaluer l'objet en lvalue → empile l'adresse de la variable qui contient le pointeur
 *   2. LOADI 1      → lit le pointeur (l'adresse de l'objet sur le tas)
 *   3. LOADL offset → offset de l'attribut
 *   4. SUBR IAdd    → adresse finale de l'attribut dans l'objet
 */
public class AttributeAssignment extends AbstractAttribute<AssignableExpression> implements AssignableExpression {

    public AttributeAssignment(AssignableExpression _object, String _name) {
        super(_object, _name);
    }

    @Override
    public Fragment getCode(TAMFactory _factory) {
        Fragment f = _factory.createFragment();
        // L'objet en lvalue renvoie l'adresse de la variable locale qui contient le pointeur
        f.append(this.object.getCode(_factory));
        // On déréférence pour obtenir le pointeur vers l'objet sur le tas
        f.add(_factory.createLoadI(1));
        // Calcule l'adresse de l'attribut = pointeur + offset
        f.add(_factory.createLoadL(this.attribute.getOffset()));
        f.add(fr.n7.stl.tam.ast.TAMFactory.createBinaryOperator(fr.n7.stl.minic.ast.expression.accessible.BinaryOperator.Add));
        return f;
    }

}
