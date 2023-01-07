package qwickie.hyperlink;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.junit.Before;
import org.junit.Test;

public class QWickieJavaHyperlinkDetectorTest {
	private final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("testproject");

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testQWickieJavaHyperlink() {
		long start = System.nanoTime();

		IFile file = project.getFile("src/main/java/org/qwickie/test/project/issue54/Issue54Page.java");
		InputStream contents;
		Document document = null;
		try {
			contents = file.getContents(true);
			byte[] b = new byte[contents.available()];
			contents.read(b);
			contents.close();
			document = new Document(new String(b));
		} catch (Exception e1) {
		}
		QWickieJavaHyperlinkDetector detector = new QWickieJavaHyperlinkDetector();
		assertNull(detector.detectHyperlinks(null, null, false));

		ITextViewer textViewer = new TextViewer(new Shell(), 0);
		assertNull(detector.detectHyperlinks(textViewer, null, false));
		assertNull(detector.detectHyperlinks(null, new Region(1, 1), false));

		textViewer.setDocument(document);
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final IWorkbenchPage activePage = workbench.getActiveWorkbenchWindow().getActivePage();
		try {
			detector.setContext(IDE.openEditor(activePage, file));
		} catch (Exception e) {
		}
		assertNull(detector.detectHyperlinks(textViewer, new Region(433, 1), false));

		for (int i = 0; i < 418; i++) {
			assertNull(detector.detectHyperlinks(textViewer, new Region(i, 1), false));
		}
		IHyperlink hyperlink = null;
		for (int i = 419; i < 431; i++) {
			IRegion region = new Region(i, 1);
			IHyperlink[] hyperlinks = detector.detectHyperlinks(textViewer, region, false);
			assertEquals(hyperlinks.length, 1);
			hyperlink = hyperlinks[0];
			assertEquals("WicketHyperlink for wicket:id \"issue54Label\" to html file", hyperlink.toString());
			assertEquals("Open html file and jump to wicket:id \"issue54Label\"", hyperlink.getHyperlinkText());
		}
		for (int i = 431; i < 440; i++) {
			assertNull(detector.detectHyperlinks(textViewer, new Region(i, 1), false));
		}

		try {
			activePage.closeAllEditors(false);
			IDE.openEditor(activePage, file);
		} catch (PartInitException e) {
		}
		hyperlink.open();
		final IEditorPart activeEditor = activePage.getActiveEditor();
		assertTrue(activeEditor.getEditorInput() instanceof FileEditorInput);
		FileEditorInput input = (FileEditorInput) activeEditor.getEditorInput();
		assertEquals("/testproject/src/main/java/org/qwickie/test/project/issue54/Issue54Page.html", input.getFile().getFullPath().toPortableString());
		activePage.closeAllEditors(false);

		System.out.println("testQWickieJavaHlDetector:\t" + (System.nanoTime() - start));
	}

	@Test
	public void testQWickieJavaHyperlinkDetector() {
		long start = System.nanoTime();

		IFile file = project.getFile("src/main/java/org/qwickie/test/project/issue45/mobile/RegistrationPage.java");
		InputStream contents;
		Document document = null;
		try {
			contents = file.getContents(true);
			byte[] b = new byte[contents.available()];
			contents.read(b);
			contents.close();
			document = new Document(new String(b));
		} catch (Exception e1) {
		}
		QWickieJavaHyperlinkDetector detector = new QWickieJavaHyperlinkDetector();
		assertNull(detector.detectHyperlinks(null, null, false));

		ITextViewer textViewer = new TextViewer(new Shell(), 0);
		assertNull(detector.detectHyperlinks(textViewer, null, false));
		assertNull(detector.detectHyperlinks(null, new Region(1, 1), false));

		textViewer.setDocument(document);
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final IWorkbenchPage activePage = workbench.getActiveWorkbenchWindow().getActivePage();
		try {
			detector.setContext(IDE.openEditor(activePage, file));
		} catch (Exception e) {
		}
		assertNull(detector.detectHyperlinks(textViewer, new Region(255, 1), false));

		IRegion region = new Region(477, 1);
		IHyperlink[] hyperlinks = detector.detectHyperlinks(textViewer, region, false);
		assertEquals(hyperlinks.length, 1);
		final IHyperlink hyperlink = hyperlinks[0];
		assertEquals("WicketHyperlink for wicket:id \"test\" to html file", hyperlink.toString());
		assertEquals("Open html file and jump to wicket:id \"test\"", hyperlink.getHyperlinkText());

		try {
			activePage.closeAllEditors(false);
			IDE.openEditor(activePage, file);
		} catch (PartInitException e) {
		}
		hyperlink.open();
		final IEditorPart activeEditor = activePage.getActiveEditor();
		assertTrue(activeEditor.getEditorInput() instanceof FileEditorInput);
		FileEditorInput input = (FileEditorInput) activeEditor.getEditorInput();
		assertEquals("/testproject/src/main/resources/org/qwickie/test/project/issue45/mobile/RegistrationPage.html",
				input.getFile().getFullPath().toPortableString());
		activePage.closeAllEditors(false);

		System.out.println("testQWickieJavaHlDetector:\t" + (System.nanoTime() - start));
	}

	@Test
	public void testQWickieJavaWicketComponent() {
		long start = System.nanoTime();

		IFile file = project.getFile("/src/main/java/org/qwickie/test/project/refactor/RefPage.java");
		InputStream contents;
		Document document = null;
		try {
			contents = file.getContents(true);
			byte[] b = new byte[contents.available()];
			contents.read(b);
			contents.close();
			document = new Document(new String(b));
		} catch (Exception e1) {
		}
		QWickieJavaHyperlinkDetector detector = new QWickieJavaHyperlinkDetector();
		assertNull(detector.detectHyperlinks(null, null, false));

		ITextViewer textViewer = new TextViewer(new Shell(), 0);
		assertNull(detector.detectHyperlinks(textViewer, null, false));
		assertNull(detector.detectHyperlinks(null, new Region(1, 1), false));

		textViewer.setDocument(document);
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final IWorkbenchPage activePage = workbench.getActiveWorkbenchWindow().getActivePage();
		try {
			detector.setContext(IDE.openEditor(activePage, file));
		} catch (Exception e) {
		}
		assertNull(detector.detectHyperlinks(textViewer, new Region(255, 1), false));

		IRegion region = new Region(1052, 1);
		IHyperlink[] hyperlinks = detector.detectHyperlinks(textViewer, region, false);
		assertEquals(2, hyperlinks.length);
		final IHyperlink wicketHyperlink = hyperlinks[0];
		final IHyperlink componentHyperlink = hyperlinks[1];
		assertEquals("WicketHyperlink for wicket:id \"content\" to html file", wicketHyperlink.toString());
		assertEquals("Open html file and jump to wicket:id \"content\"", wicketHyperlink.getHyperlinkText());
		assertEquals(
				"WicketComponentHyperlink for wicket Component ModalPanel2 to L/testproject/src/main/java/org/qwickie/test/project/refactor/ModalPanel2.html",
				componentHyperlink.toString());
		assertEquals("Open \"ModalPanel2.html\"", componentHyperlink.getHyperlinkText());

		try {
			activePage.closeAllEditors(false);
			IDE.openEditor(activePage, file);
		} catch (PartInitException e) {
		}
		wicketHyperlink.open();
		IEditorPart activeEditor = activePage.getActiveEditor();
		assertTrue(activeEditor.getEditorInput() instanceof FileEditorInput);
		FileEditorInput input = (FileEditorInput) activeEditor.getEditorInput();
		assertEquals("/testproject/src/main/java/org/qwickie/test/project/refactor/RefPage.html", input.getFile().getFullPath().toPortableString());
		activePage.closeAllEditors(false);

		componentHyperlink.open();
		activeEditor = activePage.getActiveEditor();
		assertTrue(activeEditor.getEditorInput() instanceof FileEditorInput);
		input = (FileEditorInput) activeEditor.getEditorInput();
		assertEquals("/testproject/src/main/java/org/qwickie/test/project/refactor/ModalPanel2.html", input.getFile().getFullPath().toPortableString());
		activePage.closeAllEditors(false);

		System.out.println("testQWickieJavaWicketComponent:\t" + (System.nanoTime() - start));
	}

	@Test
	public void testQWickieJWComptWithBlanks() {
		long start = System.nanoTime();

		IFile file = project.getFile("/src/main/java/org/qwickie/test/project/issue49/Issue49.java");
		InputStream contents;
		Document document = null;
		try {
			contents = file.getContents(true);
			byte[] b = new byte[contents.available()];
			contents.read(b);
			contents.close();
			document = new Document(new String(b));
		} catch (Exception e1) {
		}
		QWickieJavaHyperlinkDetector detector = new QWickieJavaHyperlinkDetector();
		assertNull(detector.detectHyperlinks(null, null, false));

		ITextViewer textViewer = new TextViewer(new Shell(), 0);
		assertNull(detector.detectHyperlinks(textViewer, null, false));
		assertNull(detector.detectHyperlinks(null, new Region(1, 1), false));

		textViewer.setDocument(document);
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final IWorkbenchPage activePage = workbench.getActiveWorkbenchWindow().getActivePage();
		try {
			detector.setContext(IDE.openEditor(activePage, file));
		} catch (Exception e) {
		}

		assertEquals("Open html file and jump to wicket:id \"a\"", detector.detectHyperlinks(textViewer, new Region(472, 1), false)[0].getHyperlinkText());
		assertEquals("Open html file and jump to wicket:id \"a\"", detector.detectHyperlinks(textViewer, new Region(473, 1), false)[0].getHyperlinkText());
		assertEquals("Open html file and jump to wicket:id \"b\"", detector.detectHyperlinks(textViewer, new Region(500, 1), false)[0].getHyperlinkText());
		assertEquals("Open html file and jump to wicket:id \"c\"", detector.detectHyperlinks(textViewer, new Region(527, 1), false)[0].getHyperlinkText());
		assertEquals("Open html file and jump to wicket:id \"c\"", detector.detectHyperlinks(textViewer, new Region(554, 1), false)[0].getHyperlinkText());
		assertEquals("Open html file and jump to wicket:id \"d\"", detector.detectHyperlinks(textViewer, new Region(582, 1), false)[0].getHyperlinkText());
		assertEquals("Open html file and jump to wicket:id \"h\"", detector.detectHyperlinks(textViewer, new Region(613, 1), false)[0].getHyperlinkText());
		assertEquals("Open html file and jump to wicket:id \"input1\"", detector.detectHyperlinks(textViewer, new Region(678, 1), false)[0].getHyperlinkText());
		assertEquals("Open html file and jump to wicket:id \"input2\"", detector.detectHyperlinks(textViewer, new Region(746, 1), false)[0].getHyperlinkText());
		assertEquals("Open html file and jump to wicket:id \"input3\"", detector.detectHyperlinks(textViewer, new Region(814, 1), false)[0].getHyperlinkText());

		try {
			activePage.closeAllEditors(false);
			IDE.openEditor(activePage, file);
		} catch (PartInitException e) {
		}
		activePage.closeAllEditors(false);
		System.out.println("testQWickieJWComptWithBlanks:\t" + (System.nanoTime() - start));
	}

}
