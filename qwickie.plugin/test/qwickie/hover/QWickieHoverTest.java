package qwickie.hover;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.junit.Before;
import org.junit.Test;

public class QWickieHoverTest {
	private final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("testproject");
	private final IFile javaFile = project.getFile("src/main/java/org/qwickie/test/project/HomePage.java");
	private IDocument javaDocument;

	@Before
	public void setUp() throws Exception {
		InputStream contents = javaFile.getContents(true);
		byte[] b = new byte[contents.available()];
		contents.read(b);
		contents.close();
		javaDocument = new Document(new String(b));
	}

	@Test
	public void testHoverInfo() {
		long start = System.nanoTime();

		QWickieHover hover = new QWickieHover();
		final IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		try {
			IDE.openEditor(activePage, javaFile);
		} catch (final PartInitException e) {
		}
		hover.setEditor(activePage.getActiveEditor());
		TextViewer viewer = new TextViewer(new Shell(), 0);
		viewer.setDocument(javaDocument);
		assertEquals("<b>Line in HomePage.html</b><br>&lt;div wicket:id=<b>\"message\"</b>>&lt;/div>", hover.getHoverInfo(viewer, new Region(432, 0)));

		activePage.closeAllEditors(false);

		System.out.println("testHoverInfo:\t\t\t" + (System.nanoTime() - start));
	}

}
