/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package qwickie.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.NamedMember;
import org.eclipse.jdt.internal.core.ResolvedBinaryMethod;
import org.eclipse.jdt.internal.core.ResolvedBinaryType;
import org.eclipse.jdt.internal.core.ResolvedSourceMethod;

/**
 * @author count.negative
 * 
 */
@SuppressWarnings("restriction")
public final class TypeHelper {
	private static final String PACKAGE_MARKUP = "org.apache.wicket.markup";
	private static final String PACKAGE_MODEL = "org.apache.wicket.model";
	public static final String COMPONENT = "org.apache.wicket.Component";

	private TypeHelper() {
	}

	public static List<Object> getSupertypes(final IFile javaFile) {
		final List<Object> list = new ArrayList<Object>();
		if (javaFile != null) {
			final ICompilationUnit icu = JavaCore.createCompilationUnitFrom(javaFile);
			try {
				final IType[] allTypes = icu.getAllTypes();
				for (final IType type : allTypes) {
					final ITypeHierarchy sth = type.newTypeHierarchy(null);
					final IType[] supertypes = sth.getAllSupertypes(type);
					for (final IType supertype : supertypes) {
						final IClassFile classFile = supertype.getClassFile();
						final IResource res = supertype.getResource();
						// only add if there's a source
						if (classFile != null && classFile.isStructureKnown()) {
							list.add(classFile);
						} else if (res != null && res instanceof IFile) {
							list.add(res);
						}
					}
				}
			} catch (final JavaModelException e1) {
			}
		}
		return list;
	}

	public static List<JavaElement> getWicketComponentTypes(final IFile javaFile) {
		Assert.isNotNull(javaFile);
		final Set<JavaElement> set = new HashSet<JavaElement>();

		final ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		parser.setResolveBindings(true);
		parser.setSource(JavaCore.createCompilationUnitFrom(javaFile));
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		cu.accept(new ASTVisitor() {

			@Override
			public boolean visit(final ClassInstanceCreation node) {
				final ITypeBinding ntb = node.resolveTypeBinding();
				final ITypeBinding nttb = node.getType().resolveBinding();
				IJavaElement javaElement = null;
				if (ntb == null || nttb == null) {
					return true;
				}
				if (ntb.getPackage().getName().startsWith(PACKAGE_MARKUP)) {
					javaElement = ntb.getJavaElement();
				}
				if (nttb.getPackage().getName().startsWith(PACKAGE_MARKUP)) {
					javaElement = nttb.getJavaElement();
				}
				if (javaElement == null) {
					ITypeBinding superclass = ntb.getSuperclass();
					while (superclass != null) {
						if (PACKAGE_MARKUP.startsWith(superclass.getPackage().getName())) {
							javaElement = ntb.getJavaElement();
							break;
						}
						superclass = superclass.getSuperclass();
					}
				}
				if (javaElement != null) {
					set.add((JavaElement) javaElement);
					final ResolvedBinaryType rbt = (ResolvedBinaryType) javaElement.getAdapter(ResolvedBinaryType.class);
					if (rbt != null) {
						set.add(rbt);
					}
				}
				return true;
			}

		});

		return new ArrayList<JavaElement>(set);
	}

	public static List<IVariableBinding> getWicketModelTypes(final IFile javaFile) {
		Assert.isNotNull(javaFile);
		final List<IVariableBinding> list = new ArrayList<IVariableBinding>();

		final ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		parser.setResolveBindings(true);
		parser.setSource(JavaCore.createCompilationUnitFrom(javaFile));
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		cu.accept(new ASTVisitor() {

			@Override
			public boolean visit(final ClassInstanceCreation node) {
				ITypeBinding ntb = node.resolveTypeBinding();
				while (ntb != null) {
					if (ntb.getPackage().getName().startsWith(PACKAGE_MODEL)) {
						// it is a wicket model
						final ITypeBinding[] typeArguments = ntb.getTypeArguments();
						for (int i = 0; i < typeArguments.length; i++) {
							ITypeBinding typeArgument = typeArguments[i];
							if (!typeArgument.isArray() && !typeArgument.isNullType() && !typeArgument.isPrimitive()
									&& !"java.lang.String".equals(typeArgument.getBinaryName())) {
								list.addAll(Arrays.asList(typeArgument.getDeclaredFields()));
								return false;
							}
						}
					}
					ntb = ntb.getSuperclass();
				}
				return true;
			}

		});

		return list;
	}

	private static boolean hierarchyContainsComponent(final IType type) throws JavaModelException {
		Assert.isNotNull(type);
		final ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
		if (hierarchy != null) {
			final IType[] supertypes = hierarchy.getAllSupertypes(type);
			for (final IType iType : supertypes) {
				if (iType.getFullyQualifiedName().equals(TypeHelper.COMPONENT)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * checks if the JavaElement extends Component and has a String (wicketId)
	 * as the first CTOR Parameter
	 * 
	 * @throws JavaModelException
	 */
	public static boolean isWicketComponent(final IJavaElement javaElement) throws JavaModelException {
		Assert.isNotNull(javaElement);
		if (javaElement instanceof ResolvedSourceMethod) {
			final ResolvedSourceMethod method = (ResolvedSourceMethod) javaElement;
			if ("QComponent;".equals(method.getReturnType())) {
				return true;
			}
			final IType type = method.getDeclaringType();
			if (type != null) {
				if (type.getFullyQualifiedName().equals(TypeHelper.COMPONENT)) {
					return true;
				}
				return hierarchyContainsComponent(type);
			}
		}
		if ((javaElement instanceof ResolvedBinaryMethod)) {
			final ResolvedBinaryMethod method = (ResolvedBinaryMethod) javaElement;
			if ("V;".equals(method.getReturnType())) {
				return true;
			}
			final IType type = method.getDeclaringType();
			if (type != null) {
				if (type.getFullyQualifiedName().equals(TypeHelper.COMPONENT)) {
					return true;
				}
				return hierarchyContainsComponent(type);
			}
		}
		if ((javaElement instanceof ResolvedBinaryType)) {
			final ResolvedBinaryType method = (ResolvedBinaryType) javaElement;
			return hierarchyContainsComponent(method);
		}
		return false;
	}

	public static boolean isWicketJavaElement(final IJavaElement javaElement) throws JavaModelException {
		Assert.isNotNull(javaElement);
		if (javaElement != null && javaElement instanceof NamedMember) {
			if (javaElement.getElementName().equals(DocumentHelper.GET_STRING)) {
				return true;
			} else if (javaElement.getElementType() == IJavaElement.TYPE) {
				final NamedMember method = (NamedMember) javaElement;
				final IType type = method.getTypeRoot().findPrimaryType();
				return hierarchyContainsComponent(type);
			} else if (javaElement.getElementType() == IJavaElement.METHOD) {
				return isWicketComponent(javaElement);
			}
			return isWicketJavaElement(javaElement.getParent());
		}
		return false;
	}

}
