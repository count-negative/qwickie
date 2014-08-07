package qwickie.quickfix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.junit.Before;
import org.junit.Test;

import qwickie.QWickieActivator;
import qwickie.builder.QWickieBuilder;
import qwickie.preferences.QWickiePreferencePage;

public class QWickieQuickFixProposalTest {
	private final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("testproject");
	private final IFile javaFile = project.getFile("src/main/java/org/qwickie/test/project/HomePage.java");
	private IMarker marker;
	private IDocument javaDocument;

	@Before
	public void setUp() throws Exception {
		marker = javaFile.createMarker(QWickieBuilder.MARKER_TYPE);
		marker.setAttribute(QWickieBuilder.MARKER_ATTRIB_WICKET_ID, "wicketId");
		marker.setAttribute(QWickieBuilder.MARKER_ATTRIB_HTML_SNIPPET, "submit");
		marker.setAttribute(IMarker.MESSAGE, "message");
		marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
		marker.setAttribute(IMarker.SEVERITY, 2);
		marker.setAttribute(IMarker.LINE_NUMBER, 12);
		marker.setAttribute(IMarker.PROBLEM, true);
		marker.setAttribute(IMarker.CHAR_START, 108);
		marker.setAttribute(IMarker.CHAR_END, 114);

		InputStream contents = javaFile.getContents(true);
		byte[] b = new byte[contents.available()];
		contents.read(b);
		contents.close();
		javaDocument = new Document(new String(b));

	}

	@Test
	public void testQuickFixProposal() {
		long start = System.nanoTime();

		try {
			QWickieQuickFixProposal proposal = new QWickieQuickFixProposal(0, 0, marker);
			QWickieActivator.getDefault().getPreferenceStore().setValue(QWickiePreferencePage.ADD_NEW_COMPONENTS, "init");
			assertEquals(proposal.getAdditionalProposalInfo(), "adds a <b>new SubmitLink(\"wicketId\");</b> to the onInitialize method");
			proposal.apply(javaDocument);
			assertTrue(javaDocument.get().contains("super.onInitialize();\nadd(new SubmitLink(\"wicketId\"));"));
			QWickieActivator.getDefault().getPreferenceStore().setValue(QWickiePreferencePage.ADD_NEW_COMPONENTS, "ctor");
			proposal.apply(javaDocument);
			assertTrue(javaDocument.get().contains("{\nadd(new SubmitLink(\"wicketId\"));"));

			assertNull(proposal.getContextInformation());
			assertNull(proposal.getSelection(javaDocument));
			assertEquals(proposal.getRelevance(), 0);
			assertNotNull(proposal.getImage());
		} catch (CoreException e) {
		}

		System.out.println("testQuickFixProposal:\t\t" + (System.nanoTime() - start));
	}

}
