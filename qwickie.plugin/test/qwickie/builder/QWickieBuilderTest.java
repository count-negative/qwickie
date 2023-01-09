package qwickie.builder;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.eclipse.core.internal.events.BuildManager;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.ILock;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction")
public class QWickieBuilderTest {

	private final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("testproject");

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testQWickieBuilder() {
		long start = System.nanoTime(); // 1975738

		ILock workspaceLock = new ILock() {

			public void release() {
			}

			public int getDepth() {
				return 0;
			}

			public boolean acquire(final long delay) throws InterruptedException {
				return false;
			}

			public void acquire() {
			}
		};

		BuildManager buildManager = new BuildManager((Workspace) ResourcesPlugin.getWorkspace(), workspaceLock);
		IBuildConfiguration buildConfiguration = new IBuildConfiguration() {

			public <T> T getAdapter(Class<T> adapter) {
				return null;
			}

			public IProject getProject() {
				return project;
			}

			public String getName() {
				return "QWickieBuilder";
			}

		};
		buildManager.build(buildConfiguration, 0, "QWickieBuilder", new HashMap<String, String>(), null);
		try {
			// mark the error in builder package BuilderPage
			final IMarker[] markers = project.findMarkers(QWickieBuilder.MARKER_TYPE, false, IResource.DEPTH_INFINITE);
			assertEquals(2, markers.length);

			IMarker javaMarker = markers[0].getResource().getName().endsWith("java") ? markers[0] : markers[1];
			assertEquals("BuilderPage.java", javaMarker.getResource().getName());
			assertEquals("wicket:id \"testDiv\" not found in java file(s)", javaMarker.getAttribute("message"));
			assertEquals("testDiv", javaMarker.getAttribute("wicketId"));
			assertEquals(Boolean.TRUE, javaMarker.getAttribute("org.eclipse.core.resources.problemmarker"));
			assertEquals(1, javaMarker.getAttribute("priority"));
			assertEquals(107, javaMarker.getAttribute("charStart"));
			assertEquals(4, javaMarker.getAttribute("lineNumber"));
			assertEquals(2, javaMarker.getAttribute("severity"));
			assertEquals(118, javaMarker.getAttribute("charEnd"));
			assertEquals("div", javaMarker.getAttribute("htmlSnippet"));

			IMarker htmlMarker = markers[0].getResource().getName().endsWith("html") ? markers[0] : markers[1];
			assertEquals("BuilderPage.html", htmlMarker.getResource().getName());
			assertEquals("wicket:id \"testDiv\" not found in java file(s)", htmlMarker.getAttribute("message"));
			assertEquals("testDiv", htmlMarker.getAttribute("wicketId"));
			assertEquals(Boolean.TRUE, htmlMarker.getAttribute("org.eclipse.core.resources.problemmarker"));
			assertEquals(1, htmlMarker.getAttribute("priority"));
			assertEquals(275, htmlMarker.getAttribute("charStart"));
			assertEquals(7, htmlMarker.getAttribute("lineNumber"));
			assertEquals(2, htmlMarker.getAttribute("severity"));
			assertEquals(282, htmlMarker.getAttribute("charEnd"));
			assertEquals("div", htmlMarker.getAttribute("htmlSnippet"));
		} catch (CoreException e) {
		}

		System.out.println("testQWickieBuilder:\t\t" + (System.nanoTime() - start));
	}

}
