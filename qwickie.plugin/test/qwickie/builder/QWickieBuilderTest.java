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

			public Object getAdapter(final Class adapter) {
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

			assertEquals("BuilderPage.java", markers[0].getResource().getName());
			assertEquals("wicket:id \"testDiv\" not found in java file(s)", markers[0].getAttribute("message"));
			assertEquals("testDiv", markers[0].getAttribute("wicketId"));
			assertEquals(Boolean.TRUE, markers[0].getAttribute("org.eclipse.core.resources.problemmarker"));
			assertEquals(1, markers[0].getAttribute("priority"));
			assertEquals(107, markers[0].getAttribute("charStart"));
			assertEquals(4, markers[0].getAttribute("lineNumber"));
			assertEquals(2, markers[0].getAttribute("severity"));
			assertEquals(118, markers[0].getAttribute("charEnd"));
			assertEquals("div", markers[0].getAttribute("htmlSnippet"));

			assertEquals("BuilderPage.html", markers[1].getResource().getName());
			assertEquals("wicket:id \"testDiv\" not found in java file(s)", markers[1].getAttribute("message"));
			assertEquals("testDiv", markers[1].getAttribute("wicketId"));
			assertEquals(Boolean.TRUE, markers[1].getAttribute("org.eclipse.core.resources.problemmarker"));
			assertEquals(1, markers[1].getAttribute("priority"));
			assertEquals(255, markers[1].getAttribute("charStart"));
			assertEquals(7, markers[1].getAttribute("lineNumber"));
			assertEquals(2, markers[1].getAttribute("severity"));
			assertEquals(262, markers[1].getAttribute("charEnd"));
			assertEquals("div", markers[1].getAttribute("htmlSnippet"));
		} catch (CoreException e) {
		}

		System.out.println("testQWickieBuilder:\t\t" + (System.nanoTime() - start));
	}

}
