package qwickie.hyperlink;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.ResourceUtil;
import org.junit.Before;
import org.junit.Test;

public class WicketHyperlinkTest {
	private final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("testproject");

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testWicketHyperlink() {
		long start = System.nanoTime();

		final Region region = new Region(1, 10);
		WicketHyperlink wh = new WicketHyperlink(region, "panel", "ext");
		assertEquals(wh.getHyperlinkText(), "Open ext file and jump to wicket:id \"panel\"");
		assertEquals(wh.toString(), "WicketHyperlink for wicket:id \"panel\" to ext file");
		assertEquals(wh.getHyperlinkRegion(), region);

		System.out.println("testWicketHyperlink:\t\t" + (System.nanoTime() - start));
	}

	@Test
	public void testWicketHyperlinkOpenHTML() throws PartInitException {
		long start = System.nanoTime();

		final IWorkbench workbench = PlatformUI.getWorkbench();
		final IWorkbenchPage activePage = workbench.getActiveWorkbenchWindow().getActivePage();
		IDE.openEditor(activePage, project.getFile("/src/main/java/org/qwickie/test/project/issue45/mobile/RegistrationPage.java"), true);

		final Region region = new Region(21 * 25, 1);
		WicketHyperlink wh = new WicketHyperlink(region, "test", "html");
		wh.open();
		final ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		final ITextSelection selection = (ITextSelection) selectionService.getSelection();
		assertEquals("test", selection.getText());
		assertEquals(7, selection.getStartLine());
		assertEquals(7, selection.getEndLine());
		final IResource resource = ResourceUtil
				.getResource(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput());
		assertEquals("RegistrationPage.html", resource.getName());

		activePage.closeAllEditors(false);

		System.out.println("testWicketHyperlinkOpenHTML:\t" + (System.nanoTime() - start));
	}

	@Test
	public void testWicketHyperlinkOpenJava() throws PartInitException {
		long start = System.nanoTime();

		final IWorkbench workbench = PlatformUI.getWorkbench();
		final IWorkbenchPage activePage = workbench.getActiveWorkbenchWindow().getActivePage();
		IDE.openEditor(activePage, project.getFile("/src/main/resources/org/qwickie/test/project/issue45/mobile/RegistrationPage.html"), true);

		final Region region = new Region(263, 1);
		WicketHyperlink wh = new WicketHyperlink(region, "test", "java");
		wh.open();
		final ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		final ITextSelection selection = (ITextSelection) selectionService.getSelection();
		assertEquals("test", selection.getText());
		assertEquals(21, selection.getStartLine());
		assertEquals(21, selection.getEndLine());
		final IResource resource = ResourceUtil
				.getResource(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput());
		assertEquals("RegistrationPage.java", resource.getName());

		activePage.closeAllEditors(false);

		System.out.println("testWicketHyperlinkOpenJava:\t" + (System.nanoTime() - start));
	}

	@Test
	public void testWicketHyperlinkCreateProps() throws PartInitException {
		long start = System.nanoTime();

		final IWorkbench workbench = PlatformUI.getWorkbench();
		final IWorkbenchPage activePage = workbench.getActiveWorkbenchWindow().getActivePage();
		IDE.openEditor(activePage, project.getFile("/src/main/java/org/qwickie/test/project/getstring/GetString.java"), true);

		final Region region = new Region(763, 1);
		WicketHyperlink wh = new WicketHyperlink(region, "firstname", "properties");

		wh.open();
		final ISelectionService selectionService = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
		final ITextSelection selection = (ITextSelection) selectionService.getSelection();
		assertEquals("Your first name", selection.getText());
		assertEquals(0, selection.getStartLine());
		assertEquals(0, selection.getEndLine());
		final IResource resource = ResourceUtil
				.getResource(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor().getEditorInput());
		assertEquals("wicket-package.properties", resource.getName());

		activePage.closeAllEditors(false);

		System.out.println("testWicketHyperlinkCreateProps:\t" + (System.nanoTime() - start));
	}

	@Test
	public void testWicketHyperlinkDetector() {
		long start = System.nanoTime();

		//		QWickieJavaHyperlinkDetector detector = new QWickieJavaHyperlinkDetector();
		//		detector.setContext(project);
		//		TextViewer textViewer = new TextViewer(new Shell(), 0);
		//		textViewer.setDocument(javaDocument);
		//		Region region = new Region(100, 10);
		//		final IHyperlink[] hyperlinks = detector.detectHyperlinks(textViewer, region, false);

		System.out.println("testWicketHyperlinkDetector:\t" + (System.nanoTime() - start));
	}

}
