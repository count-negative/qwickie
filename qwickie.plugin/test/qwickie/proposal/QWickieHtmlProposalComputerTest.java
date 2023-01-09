package qwickie.proposal;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.junit.Before;
import org.junit.Test;

public class QWickieHtmlProposalComputerTest {
	final IWorkbench workbench = PlatformUI.getWorkbench();
	final IWorkbenchPage activePage = workbench.getActiveWorkbenchWindow().getActivePage();
	private final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("testproject");
	private final IFile htmlFile = project.getFile("src/main/java/org/qwickie/test/project/HomePage.html");
	private IDocument htmlDocument;

	@Before
	public void setUp() throws Exception {
		InputStream contents = htmlFile.getContents(true);
		byte[] b = new byte[contents.available()];
		contents.read(b);
		contents.close();
		htmlDocument = new Document(new String(b));
	}

	@Test
	public void testHtmlProposal() {
		long start = System.nanoTime();

		QWickieHtmlProposalComputer proposal = new QWickieHtmlProposalComputer();
		proposal.sessionStarted();

		ITextViewer viewer = new TextViewer(new Shell(), 0);
		viewer.setDocument(htmlDocument);
		try {
			IDE.openEditor(activePage, htmlFile);
		} catch (PartInitException e) {
		}

		CompletionProposalInvocationContext context = new CompletionProposalInvocationContext(viewer, 0);
		IProgressMonitor monitor = new NullProgressMonitor();
		assertEquals(proposal.computeCompletionProposals(context, monitor).size(), 0);

		context = new CompletionProposalInvocationContext(viewer, 539);
		List<CompletionProposal> proposals = proposal.computeCompletionProposals(context, monitor);
		assertEquals(7, proposals.size());

		assertEquals("customer", proposals.get(0).getDisplayString());
		assertEquals("message", proposals.get(1).getDisplayString());
		assertEquals("some.message", proposals.get(2).getDisplayString());
		assertEquals("message2", proposals.get(3).getDisplayString());
		assertEquals("message 2", proposals.get(4).getDisplayString());
		assertEquals("testMessageInXML", proposals.get(5).getDisplayString());
		assertEquals("date", proposals.get(6).getDisplayString());

		activePage.closeAllEditors(false);

		proposal.sessionEnded();

		System.out.println("testHtmlProposal:\t\t" + (System.nanoTime() - start));
	}

}
