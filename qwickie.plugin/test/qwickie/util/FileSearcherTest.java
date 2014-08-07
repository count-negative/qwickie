package qwickie.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Before;
import org.junit.Test;

public class FileSearcherTest {
	IProject[] projects;
	IProject project;

	@Before
	public void setUp() throws Exception {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		if (projects.length == 0) {
			throw new Exception("no projects found in test workspace");
		}
		project = ResourcesPlugin.getWorkspace().getRoot().getProject("testproject");
	}

	@Test
	public void testGetFoundFiles() {
		long start = System.nanoTime();

		FileSearcher fs = new FileSearcher(project, "HomePage.java");
		try {
			project.accept(fs);
		} catch (CoreException e) {
		}
		List<IFile> foundFiles = fs.getFoundFiles();
		assertEquals(foundFiles.size(), 2);
		assertEquals(foundFiles.get(0).getFullPath().toPortableString(), "/testproject/src/main/java/org/qwickie/test/project/xml/HomePage.java");
		assertEquals(foundFiles.get(1).getFullPath().toPortableString(), "/testproject/src/main/java/org/qwickie/test/project/HomePage.java");

		fs = new FileSearcher(project, "HomePage*");
		try {
			project.accept(fs);
		} catch (CoreException e) {
		}
		foundFiles = fs.getFoundFiles();
		assertEquals(foundFiles.size(), 8);
		assertEquals(foundFiles.get(0).getFullPath().toPortableString(), "/testproject/src/main/java/org/qwickie/test/project/HomePage_us_1.properties");
		assertEquals(foundFiles.get(1).getFullPath().toPortableString(), "/testproject/src/main/java/org/qwickie/test/project/HomePage_us_1.html");
		assertEquals(foundFiles.get(2).getFullPath().toPortableString(), "/testproject/src/main/java/org/qwickie/test/project/HomePage_en.properties");
		assertEquals(foundFiles.get(3).getFullPath().toPortableString(), "/testproject/src/main/java/org/qwickie/test/project/HomePage_en.html");
		assertEquals(foundFiles.get(4).getFullPath().toPortableString(), "/testproject/src/main/java/org/qwickie/test/project/HomePage_de_1.properties");
		assertEquals(foundFiles.get(5).getFullPath().toPortableString(), "/testproject/src/main/java/org/qwickie/test/project/HomePage_de_1.html");
		assertEquals(foundFiles.get(6).getFullPath().toPortableString(), "/testproject/src/main/java/org/qwickie/test/project/HomePage_de.properties");
		assertEquals(foundFiles.get(7).getFullPath().toPortableString(), "/testproject/src/main/java/org/qwickie/test/project/HomePage_de.html");

		System.out.println("testGetFoundFiles:\t\t" + (System.nanoTime() - start));
	}

	@Test
	public void testGetFoundFile() {
		long start = System.nanoTime();

		FileSearcher fs = new FileSearcher(project, "HomePage.java");
		try {
			project.accept(fs);
		} catch (CoreException e) {
		}
		IFile foundFile = fs.getFoundFile();
		assertNotNull(foundFile);
		assertEquals(foundFile.getFullPath().toPortableString(), "/testproject/src/main/java/org/qwickie/test/project/xml/HomePage.java");

		System.out.println("testGetFoundFile:\t\t" + (System.nanoTime() - start));
	}

	@Test
	public void testGetSourceFolders() {
		long start = System.nanoTime();

		final List<IPath> sourceFolders = FileSearcher.getSourceFolders(project);
		assertEquals(sourceFolders.size(), 4);
		assertTrue(sourceFolders.contains(new Path("/testproject/src/main/java")));
		assertTrue(sourceFolders.contains(new Path("/testproject/src/main/resources")));
		assertTrue(sourceFolders.contains(new Path("/testproject/src/test/java")));
		assertTrue(sourceFolders.contains(new Path("/testproject/src/test/resources")));

		System.out.println("testGetSourceFolders:\t\t" + (System.nanoTime() - start));
	}

	@Test
	public void testRemoveSourceFolder() {
		long start = System.nanoTime();

		assertEquals("/org/qwickie/test/project/issue45/mobile/RegistrationPage.java", FileSearcher.removeSourceFolder(project, "/testproject/src/main/java/org/qwickie/test/project/issue45/mobile/RegistrationPage.java"));
		assertEquals("/org/qwickie/test/project/issue45/web/RegistrationPage.java", FileSearcher.removeSourceFolder(project, "/testproject/src/main/java/org/qwickie/test/project/issue45/web/RegistrationPage.java"));
		assertEquals("/org/qwickie/test/project/issue45/mobile/RegistrationPage.html", FileSearcher.removeSourceFolder(project, "/testproject/src/main/resources/org/qwickie/test/project/issue45/mobile/RegistrationPage.html"));
		assertEquals("/org/qwickie/test/project/issue45/web/RegistrationPage.html", FileSearcher.removeSourceFolder(project, "/testproject/src/main/resources/org/qwickie/test/project/issue45/web/RegistrationPage.html"));

		System.out.println("testRemoveSourceFolder:\t\t" + (System.nanoTime() - start));
	}

	@Test
	public void testGetPropertiesFile() {
		long start = System.nanoTime();

		FileSearcher fs = new FileSearcher(project, "HomePage_de_1.properties");
		try {
			project.accept(fs);
		} catch (CoreException e) {
		}
		IFile foundFile = fs.getFoundFile();
		assertNotNull(foundFile);
		assertEquals(foundFile.getFullPath().toPortableString(), "/testproject/src/main/java/org/qwickie/test/project/HomePage_de_1.properties");

		System.out.println("testGetPropertiesFile:\t\t" + (System.nanoTime() - start));
	}

}
