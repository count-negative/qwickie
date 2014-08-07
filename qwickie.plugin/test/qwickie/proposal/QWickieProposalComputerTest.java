package qwickie.proposal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.InputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.ResolvedBinaryType;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;

public class QWickieProposalComputerTest {
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
	public void testProposal() {
		long start = System.nanoTime();

		QWickieProposalComputer proposal = new QWickieProposalComputer();
		proposal.sessionStarted();
		ContentAssistInvocationContext context = new ContentAssistInvocationContext(javaDocument, 0);
		IProgressMonitor monitor = new NullProgressMonitor();
		assertEquals(proposal.computeCompletionProposals(context, monitor).size(), 0);

		final ICompilationUnit cu = JavaCore.createCompilationUnitFrom(javaFile);
		ITextViewer viewer = new TextViewer(new Shell(), 0);
		int offset = 420;
		context = new JavaContentAssistInvocationContext(viewer, offset, new CompilationUnitEditor()) {
			@SuppressWarnings("restriction")
			@Override
			public IType getExpectedType() {
				JavaElement je = null;
				return new ResolvedBinaryType(je, "String", "java.lang.String");
			}

			@Override
			public IDocument getDocument() {
				String content = javaDocument.get();
				javaDocument.set(content.replace("message", "messag"));
				return javaDocument;
			}

			@Override
			public ICompilationUnit getCompilationUnit() {
				return cu;
			}
		};

		List<ICompletionProposal> proposals = proposal.computeCompletionProposals(context, monitor);
		assertEquals(2, proposals.size());
		assertEquals("&lt;div wicket:id=\"<b>message</b>\">&lt;/div>", proposals.get(0).getAdditionalProposalInfo());
		assertEquals("message", proposals.get(0).getDisplayString());
		assertEquals(415, proposals.get(0).getSelection(javaDocument).x);
		assertEquals("&lt;div wicket:id=\"<b>message2</b>\">&lt;/div>", proposals.get(1).getAdditionalProposalInfo());
		assertEquals("message2", proposals.get(1).getDisplayString());
		assertEquals(415, proposals.get(1).getSelection(javaDocument).x);

		offset = 421;
		context = new ContentAssistInvocationContext(viewer, offset) {
			@Override
			public IDocument getDocument() {
				return javaDocument;
			}
		};
		assertEquals(proposal.computeCompletionProposals(context, monitor).size(), 2);

		assertNull(proposal.getErrorMessage());
		assertNull(proposal.computeContextInformation(context, monitor));
		proposal.sessionEnded();

		System.out.println("testProposal:\t\t\t" + (System.nanoTime() - start));
	}

}
