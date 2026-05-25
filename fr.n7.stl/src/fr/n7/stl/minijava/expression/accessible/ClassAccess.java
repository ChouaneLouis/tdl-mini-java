package fr.n7.stl.minijava.expression.accessible;

import fr.n7.stl.minic.ast.expression.AbstractAccess;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;

public class ClassAccess extends AbstractAccess {

	private ClassDeclaration classDecl;

	public ClassAccess(ClassDeclaration classDecl) {
		this.classDecl = classDecl;
	}

	@Override
	protected fr.n7.stl.minic.ast.scope.Declaration getDeclaration() {
		return this.classDecl;
	}

	@Override
	public Type getType() {
		return new ClassType(classDecl);
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		// A class doesn't have an address value
		return _factory.createFragment();
	}
}
