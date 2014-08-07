package qwickie.quickfix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import qwickie.builder.QWickieBuilder;

public class QWickieQuickFixProcessorTest {
	private final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("testproject");
	private final IFile javaFile = project.getFile("src/main/java/org/qwickie/test/project/panel/CustomerPanel.java");

	@Before
	public void setUp() throws Exception {
		final IMarker marker = javaFile.createMarker(QWickieBuilder.MARKER_TYPE);
		marker.setAttribute(QWickieBuilder.MARKER_ATTRIB_WICKET_ID, "wicketId");
		marker.setAttribute(QWickieBuilder.MARKER_ATTRIB_HTML_SNIPPET, "submit");
		marker.setAttribute(IMarker.MESSAGE, "message");
		marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
		marker.setAttribute(IMarker.SEVERITY, 2);
		marker.setAttribute(IMarker.LINE_NUMBER, 12);
		marker.setAttribute(IMarker.PROBLEM, true);
		marker.setAttribute(IMarker.CHAR_START, 108);
		marker.setAttribute(IMarker.CHAR_END, 114);
	}

	@After
	public void tearDown() {
		try {
			javaFile.deleteMarkers(QWickieBuilder.MARKER_TYPE, true, 0);
		} catch (CoreException e) {
		}
	}

	@Test
	public void testQuickFixProcessor() {
		long start = System.nanoTime();

		QWickieQuickFixProcessor processor = new QWickieQuickFixProcessor();
		IInvocationContext context = new IInvocationContext() {

			public int getSelectionOffset() {
				return 10;
			}

			public int getSelectionLength() {
				return 10;
			}

			public ASTNode getCoveringNode() {
				return null;
			}

			public ASTNode getCoveredNode() {
				return null;
			}

			public ICompilationUnit getCompilationUnit() {
				return JavaCore.createCompilationUnitFrom(javaFile);
			}

			public CompilationUnit getASTRoot() {
				return null;
			}
		};
		IProblemLocation location = new IProblemLocation() {

			public boolean isError() {
				return false;
			}

			public int getProblemId() {
				return 0;
			}

			public String[] getProblemArguments() {
				return null;
			}

			public int getOffset() {
				return 0;
			}

			public String getMarkerType() {
				return null;
			}

			public int getLength() {
				return 0;
			}

			public ASTNode getCoveringNode(final CompilationUnit arg0) {
				return null;
			}

			public ASTNode getCoveredNode(final CompilationUnit arg0) {
				return null;
			}
		};
		IProblemLocation[] locations = new IProblemLocation[] { location };
		try {
			final IJavaCompletionProposal[] corrections = processor.getCorrections(context, locations);
			assertEquals(corrections.length, 1);
			assertEquals(corrections[0].getDisplayString(), "add new SubmitLink(\"wicketId\")");
		} catch (CoreException e) {
		}

		assertFalse(processor.hasCorrections(context.getCompilationUnit(), 1));

		System.out.println("testQuickFixProcessor:\t\t" + (System.nanoTime() - start));
	}
}
