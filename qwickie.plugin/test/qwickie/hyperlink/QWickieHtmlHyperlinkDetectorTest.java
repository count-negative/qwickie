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
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.junit.Before;
import org.junit.Test;

public class QWickieHtmlHyperlinkDetectorTest {
	private final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("testproject");

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testDetectionPackageOne() {
		long start = System.nanoTime();

		IFile file = project.getFile("src/main/resources/org/qwickie/test/project/issue45/mobile/RegistrationPage.html");
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
		QWickieHtmlHyperlinkDetector detector = new QWickieHtmlHyperlinkDetector();

		IHyperlink[] hls = detector.detectHyperlinks(null, null, false);
		assertNull(hls);

		ITextViewer textViewer = new TextViewer(new Shell(), 0);
		hls = detector.detectHyperlinks(textViewer, null, false);
		assertNull(hls);

		hls = detector.detectHyperlinks(textViewer, new Region(10, 1), false);
		textViewer.setDocument(document);
		assertNull(hls);

		hls = detector.detectHyperlinks(textViewer, new Region(10, 1), false);
		assertNull(hls);

		IRegion region = new Region(255, 1);
		final IHyperlink[] hyperlinks = detector.detectHyperlinks(textViewer, region, false);
		assertEquals(hyperlinks.length, 1);
		final IHyperlink hyperlink = hyperlinks[0];
		assertEquals(hyperlink.getHyperlinkText(), "Open java file and jump to wicket:id \"test\"");

		final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			activePage.closeAllEditors(false);
			IDE.openEditor(activePage, file);
		} catch (PartInitException e) {
		}
		hyperlink.open();
		final IEditorPart activeEditor = activePage.getActiveEditor();
		assertTrue(activeEditor.getEditorInput() instanceof FileEditorInput);
		FileEditorInput input = (FileEditorInput) activeEditor.getEditorInput();
		assertEquals(input.getFile().getFullPath().toPortableString(),
				"/testproject/src/main/java/org/qwickie/test/project/issue45/mobile/RegistrationPage.java");
		activePage.closeAllEditors(false);

		System.out.println("testDetectionPackageOne:\t" + (System.nanoTime() - start));
	}

	@Test
	public void testDetectionPackageTwo() {
		long start = System.nanoTime();

		IFile file = project.getFile("src/main/resources/org/qwickie/test/project/issue45/web/RegistrationPage.html");
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
		QWickieHtmlHyperlinkDetector detector = new QWickieHtmlHyperlinkDetector();
		ITextViewer textViewer = new TextViewer(new Shell(), 0);
		textViewer.setDocument(document);
		IRegion region = new Region(255, 1);
		final IHyperlink[] hyperlinks = detector.detectHyperlinks(textViewer, region, false);
		assertEquals(hyperlinks.length, 1);
		final IHyperlink hyperlink = hyperlinks[0];
		assertEquals(hyperlink.getHyperlinkText(), "Open java file and jump to wicket:id \"test\"");

		final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			activePage.closeAllEditors(false);
			IDE.openEditor(activePage, file);
		} catch (PartInitException e) {
		}
		hyperlink.open();
		final IEditorPart activeEditor = activePage.getActiveEditor();
		assertTrue(activeEditor.getEditorInput() instanceof FileEditorInput);
		FileEditorInput input = (FileEditorInput) activeEditor.getEditorInput();
		assertEquals(input.getFile().getFullPath().toPortableString(), "/testproject/src/main/java/org/qwickie/test/project/issue45/web/RegistrationPage.java");
		activePage.closeAllEditors(false);

		System.out.println("testDetectionPackageTwo:\t" + (System.nanoTime() - start));
	}

	@Test
	public void testDetectionXML() {
		long start = System.nanoTime();

		IFile file = project.getFile("src/main/java/org/qwickie/test/project/xml/HomePage.html");
		InputStream contents;
		Document document = null;
		String documentText = null;
		try {
			contents = file.getContents(true);
			byte[] b = new byte[contents.available()];
			contents.read(b);
			contents.close();
			documentText = new String(b);
			document = new Document(documentText);
		} catch (Exception e1) {
		}
		QWickieHtmlHyperlinkDetector detector = new QWickieHtmlHyperlinkDetector();
		ITextViewer textViewer = new TextViewer(new Shell(), 0);
		textViewer.setDocument(document);
		IRegion region = new Region(309, 1);
		final IHyperlink[] hyperlinks = detector.detectHyperlinks(textViewer, region, false);
		assertEquals(hyperlinks.length, 1);
		final IHyperlink hyperlink = hyperlinks[0];
		assertEquals(hyperlink.getHyperlinkText(), "Open properties file and jump to wicket:id \"page.test.string\"");

		final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			activePage.closeAllEditors(false);
			IDE.openEditor(activePage, file);
		} catch (PartInitException e) {
		}
		hyperlink.open();
		final IEditorPart activeEditor = activePage.getActiveEditor();
		assertTrue(activeEditor.getEditorInput() instanceof FileEditorInput);
		assertEquals(activeEditor.getClass().getCanonicalName(), "org.eclipse.wst.xml.ui.internal.tabletree.XMLMultiPageEditorPart");
		FileEditorInput input = (FileEditorInput) activeEditor.getEditorInput();
		assertEquals(input.getFile().getFullPath().toPortableString(), "/testproject/src/main/java/org/qwickie/test/project/xml/HomePage.properties.xml");
		activePage.closeAllEditors(false);

		System.out.println("testDetectionXML:\t\t" + (System.nanoTime() - start));
	}

}
