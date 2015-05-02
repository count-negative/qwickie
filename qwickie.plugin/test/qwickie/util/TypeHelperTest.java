package qwickie.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.JavaElement;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction")
public class TypeHelperTest {
	private final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("testproject");
	private final IFile javaFile = project.getFile("src/main/java/org/qwickie/test/project/panel/CustomerPanel.java");

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGetSupertypes() {
		long start = System.nanoTime();

		final List<Object> supertypes = TypeHelper.getSupertypes(javaFile);
		// if wicket 6 supertypes == 19, if wicket 1.5 then 20
		boolean size = supertypes.size()==19 || supertypes.size()==20;
		assertTrue(size);
		final ClassFile clazz = (ClassFile) supertypes.get(0);
		assertEquals(clazz.getElementName(), "FormComponentPanel.class");

		System.out.println("testGetSupertypes:\t\t" + (System.nanoTime() - start));
	}

	@Test
	public void testGetWicketComponentTypes() {
		long start = System.nanoTime();

		final List<JavaElement> wicketComponentTypes = TypeHelper.getWicketComponentTypes(javaFile);
		List<String> elems = new ArrayList<String>();
		for (JavaElement wct : wicketComponentTypes) {
			elems.add(wct.getElementName());
		}
		Collections.sort(elems);
		assertEquals(elems.get(0), "DateTextField");
		assertEquals(elems.get(1), "Form");
		assertEquals(elems.get(2), "FormComponentFeedbackIndicator");
		assertEquals(elems.get(3), "TextField");

		System.out.println("testGetWicketComponentTypes:\t" + (System.nanoTime() - start));
	}

	@Test
	public void testGetWicketModelTypes() {
		long start = System.nanoTime();

		final List<IVariableBinding> wicketModelTypes = TypeHelper.getWicketModelTypes(javaFile);
		assertEquals(wicketModelTypes.size(), 4);
		assertEquals(wicketModelTypes.get(0).toString(), "private java.util.Date birthday");
		assertEquals(wicketModelTypes.get(1).toString(), "private java.lang.String firstname");
		assertEquals(wicketModelTypes.get(2).toString(), "private java.lang.String lastname");
		assertEquals(wicketModelTypes.get(3).toString(), "private static final long serialVersionUID");

		System.out.println("testGetWicketModelTypes:\t" + (System.nanoTime() - start));
	}

	@Test
	public void testIsWicketComponent() {
		long start = System.nanoTime();

		final List<JavaElement> wicketComponentTypes = TypeHelper.getWicketComponentTypes(javaFile);
		try {
			assertTrue(TypeHelper.isWicketComponent(wicketComponentTypes.get(0)));
		} catch (JavaModelException e) {
		}

		System.out.println("testIsWicketComponent:\t\t" + (System.nanoTime() - start));
	}

	@Test
	public void testIsWicketJavaElement() {
		long start = System.nanoTime();

		final List<JavaElement> wicketComponentTypes = TypeHelper.getWicketComponentTypes(javaFile);
		try {
			assertTrue(TypeHelper.isWicketJavaElement(wicketComponentTypes.get(0)));
		} catch (JavaModelException e) {
		}

		System.out.println("testIsWicketJavaElement:\t" + (System.nanoTime() - start));
	}

}
