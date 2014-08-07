package qwickie.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.junit.Before;
import org.junit.Test;

public class DocumentHelperTest {
	private IDocument javaDocument;
	private IDocument customerDocument;
	private IDocument panelDocument;
	private IDocument htmlDocument;
	private final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("testproject");
	private final IFile javaFile = project.getFile("src/main/java/org/qwickie/test/project/HomePage.java");
	private final IFile customerFile = project.getFile("src/main/java/org/qwickie/test/project/panel/CustomerPanel.java");
	private final IFile panelFile = project.getFile("src/main/java/org/qwickie/test/project/panel/FieldsPanel.java");
	private final IFile htmlFile = project.getFile("src/main/java/org/qwickie/test/project/panel/CustomerPanel.html");

	@Before
	public void setUp() throws Exception {
		InputStream contents = javaFile.getContents(true);
		byte[] b = new byte[contents.available()];
		contents.read(b);
		contents.close();
		javaDocument = new Document(new String(b));

		contents = customerFile.getContents(true);
		b = new byte[contents.available()];
		contents.read(b);
		contents.close();
		customerDocument = new Document(new String(b));

		contents = panelFile.getContents(true);
		b = new byte[contents.available()];
		contents.read(b);
		contents.close();
		panelDocument = new Document(new String(b));

		contents = htmlFile.getContents(true);
		b = new byte[contents.available()];
		contents.read(b);
		contents.close();
		htmlDocument = new Document(new String(b));
	}

	@Test
	public void testFindStringArgumentInJava() {
		long start = System.nanoTime();

		// searching for new Label("customer")
		assertEquals(DocumentHelper.findStringArgumentInJava(javaDocument, 385), new Region(385, 8));
		assertEquals(DocumentHelper.findStringArgumentInJava(javaDocument, 386), new Region(385, 8));
		assertEquals(DocumentHelper.findStringArgumentInJava(javaDocument, 387), new Region(385, 8));
		assertEquals(DocumentHelper.findStringArgumentInJava(javaDocument, 388), new Region(385, 8));
		assertEquals(DocumentHelper.findStringArgumentInJava(javaDocument, 389), new Region(385, 8));
		assertEquals(DocumentHelper.findStringArgumentInJava(javaDocument, 390), new Region(385, 8));
		assertEquals(DocumentHelper.findStringArgumentInJava(javaDocument, 391), new Region(385, 8));
		assertEquals(DocumentHelper.findStringArgumentInJava(javaDocument, 392), new Region(385, 8));

		System.out.println("testFindStringArgumentInJava:\t" + (System.nanoTime() - start));
	}

	@Test
	public void testGetNamespacePrefix() throws Exception {
		long start = System.nanoTime();

		final String namespacePrefix = DocumentHelper.getNamespacePrefix(htmlDocument);
		assertEquals(namespacePrefix, "wicket");

		System.out.println("testGetNamespacePrefix:\t\t" + (System.nanoTime() - start));
	}

	@Test
	public void testGetWicketNamespace() {
		long start = System.nanoTime();

		String namespacePrefix = DocumentHelper.getWicketNamespace("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:wicket=\"http://wicket.apache.org/dtds.data/wicket-xhtml1.4-strict.dtd\" xml:lang=\"da\" lang=\"da\">");
		assertEquals(namespacePrefix, "wicket");
		namespacePrefix = DocumentHelper.getWicketNamespace("<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:pick=\"http://wicket.apache.org/dtds.data/wicket-xhtml1.4-strict.dtd\" xml:lang=\"da\" lang=\"da\">");
		assertEquals(namespacePrefix, "pick");
		namespacePrefix = DocumentHelper.getWicketNamespace("<html xmlns=\"http://www.w3.org/1999/xhtml\"  \r\n" + "      xmlns:lol=\"http://wicket.apache.org/dtds.data/wicket-xhtml1.3-strict.dtd\"  \r\n" + "      xml:lang=\"en\"  \r\n" + "      lang=\"en\"> ");
		assertEquals(namespacePrefix, "lol");

		System.out.println("testGetWicketNamespace:\t\t" + (System.nanoTime() - start));
	}

	@Test
	public void testFindStringArgumentInMarkup() {
		long start = System.nanoTime();

		// searching for birthday
		for (int i = 0; i < 1000; i++) {
			assertEquals(DocumentHelper.findStringArgumentInMarkup(htmlDocument, 618, "wicket:id"), new Region(618, 8));
			assertEquals(DocumentHelper.findStringArgumentInMarkup(htmlDocument, 619, "wicket:id"), new Region(618, 8));
			assertEquals(DocumentHelper.findStringArgumentInMarkup(htmlDocument, 620, "wicket:id"), new Region(618, 8));
			assertEquals(DocumentHelper.findStringArgumentInMarkup(htmlDocument, 621, "wicket:id"), new Region(618, 8));
			assertEquals(DocumentHelper.findStringArgumentInMarkup(htmlDocument, 622, "wicket:id"), new Region(618, 8));
			assertEquals(DocumentHelper.findStringArgumentInMarkup(htmlDocument, 623, "wicket:id"), new Region(618, 8));
			assertEquals(DocumentHelper.findStringArgumentInMarkup(htmlDocument, 624, "wicket:id"), new Region(618, 8));
			assertEquals(DocumentHelper.findStringArgumentInMarkup(htmlDocument, 625, "wicket:id"), new Region(618, 8));
		}

		System.out.println("testFindStringArgumentInMarkup:\t" + (System.nanoTime() - start));
	}

	@Test
	public void testFindWicketRegions() {
		long start = System.nanoTime();

		try {
			IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), javaFile);
		} catch (PartInitException e) {
		}
		final IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		assertNull(DocumentHelper.findWicketRegions(activeEditor, javaDocument, 0));
		final IRegion[] regions = DocumentHelper.findWicketRegions(activeEditor, javaDocument, 415);
		assertEquals(regions.length, 3);
		assertEquals(regions[0], new Region(415, 7));
		assertEquals(regions[1], new Region(408, 5));

		System.out.println("testFindWicketRegions:\t\t" + (System.nanoTime() - start));
	}

	@Test
	public void testGetRegionOfWicketComponent() throws Exception {
		long start = System.nanoTime();

		//		IEditorPart editor = new CompilationUnitEditor();
		//		final IJavaElement input = EditorUtility.getEditorInputJavaElement(editor , false);
		//		final IJavaElement[] javaElements = ((ICodeAssist) input).codeSelect(1343, 8);
		//		System.out.println(javaElements);
		//		JavaElement je = new ResolvedSourceMethod(parent, "newPanel", new String[] { "QString;" }, "Lcom/test/test/HomePage;.newPanel(Ljava/lang/String;)Lorg/apache/wicket/Component;");
		//		final IRegion rowc = DocumentHelper.getRegionOfWicketComponent(javaDocument, 730, je);
		//		System.out.println(rowc);

		System.out.println("testGetRegionOfWicketComponent:\t" + (System.nanoTime() - start));
	}

	@Test
	public void testFindWicketComponentRegion() {
		long start = System.nanoTime();

		final int TARGET_OFFSET = 1327;
		final int SOURCE_OFFSET = 1341;
		final IRegion region = DocumentHelper.findWicketComponentRegion(panelDocument, SOURCE_OFFSET);
		try {
			assertEquals(panelDocument.get(region.getOffset(), region.getLength()), "C");
		} catch (BadLocationException e) {
		}
		assertEquals(region, new Region(TARGET_OFFSET, 1));
		assertEquals(DocumentHelper.findWicketComponentRegion(panelDocument, SOURCE_OFFSET + 1), new Region(TARGET_OFFSET, 1));
		assertEquals(DocumentHelper.findWicketComponentRegion(panelDocument, SOURCE_OFFSET + 2), new Region(TARGET_OFFSET, 1));
		assertEquals(DocumentHelper.findWicketComponentRegion(panelDocument, SOURCE_OFFSET + 3), new Region(TARGET_OFFSET, 1));
		assertEquals(DocumentHelper.findWicketComponentRegion(panelDocument, SOURCE_OFFSET + 4), new Region(TARGET_OFFSET, 1));
		assertEquals(DocumentHelper.findWicketComponentRegion(panelDocument, SOURCE_OFFSET + 5), new Region(TARGET_OFFSET, 1));
		assertEquals(DocumentHelper.findWicketComponentRegion(panelDocument, SOURCE_OFFSET + 6), new Region(TARGET_OFFSET, 1));
		assertEquals(DocumentHelper.findWicketComponentRegion(panelDocument, SOURCE_OFFSET + 7), new Region(TARGET_OFFSET, 1));
		assertEquals(DocumentHelper.findWicketComponentRegion(panelDocument, SOURCE_OFFSET + 8), new Region(TARGET_OFFSET, 1));

		final IRegion customerRegion = DocumentHelper.findWicketComponentRegion(customerDocument, 973);
		try {
			assertEquals(customerDocument.get(customerRegion.getOffset(), customerRegion.getLength()), "F");
		} catch (BadLocationException e) {
		}

		System.out.println("testFindWicketComponentRegion:\t" + (System.nanoTime() - start));
	}

	@Test
	public void testStringConstantName() {
		long start = System.nanoTime();

		assertEquals(DocumentHelper.getStringConstantName(""), null);
		assertEquals(DocumentHelper.getStringConstantName("String g=\"dsfdfs\""), "g");
		assertEquals(DocumentHelper.getStringConstantName("String g =\"dsfdfs\""), "g");
		assertEquals(DocumentHelper.getStringConstantName("String g = \"dsfdfs\""), "g");
		assertEquals(DocumentHelper.getStringConstantName("String \ng = \"dsfdfs\""), "g");

		System.out.println("testStringConstantName:\t\t" + (System.nanoTime() - start));
	}

}
