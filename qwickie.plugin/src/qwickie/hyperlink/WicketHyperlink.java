/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package qwickie.hyperlink;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.actions.OpenNewClassWizardAction;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import qwickie.QWickieActivator;
import qwickie.util.DocumentHelper;
import qwickie.util.FileSearcher;
import qwickie.util.TypeHelper;

/**
 * @author count.negative
 *
 */
public class WicketHyperlink implements IHyperlink {

	public static final String JAVA = "java";
	public static final String HTML = "html";
	public static final String PROPERTIES = "properties";
	public static final String XML = "xml";
	public static final String XML_PROPERTIES = "properties.xml";

	private String wicketId;
	private final IRegion region;
	private final String extension;
	private boolean found;
	private boolean openJavaFileOnly;

	public WicketHyperlink(final IRegion region, final String wicketId, final String extension) {
		Assert.isNotNull(wicketId);
		Assert.isNotNull(region);
		Assert.isNotNull(extension);

		this.region = region;
		this.wicketId = wicketId;
		this.extension = extension;
	}

	public IRegion getHyperlinkRegion() {
		return this.region;
	}

	public void open() {
		if (this.wicketId != null) {
			final IWorkbench workbench = PlatformUI.getWorkbench();
			final IWorkbenchPage activePage = workbench.getActiveWorkbenchWindow().getActivePage();
			final IEditorInput editorInput = activePage.getActiveEditor().getEditorInput();
			final IResource openedResource = (IResource) editorInput.getAdapter(IResource.class);
			final List<String> toOpenFilenames = getFilenamesToOpen(openedResource, extension);
			boolean foundInPropertiesFile = false;

			for (final String toOpenFilename : toOpenFilenames) {
				if (!samePackage(FileSearcher.removeSourceFolder(openedResource.getProject(), openedResource.getFullPath().toPortableString()),
						FileSearcher.removeSourceFolder(openedResource.getProject(), toOpenFilename))) {
					continue;
				}
				try {
					// try to get the file
					final IFile file = getFile(toOpenFilename);
					if (file != null && file.exists()) {
						// open in editor
						final IEditorPart editor = IDE.openEditor(activePage, file, false);
						Assert.isNotNull(editor);

						final ITextEditor textEditor = (ITextEditor) editor.getAdapter(ITextEditor.class);
						final IDocument document = ((textEditor).getDocumentProvider()).getDocument(editor.getEditorInput());
						Assert.isNotNull(document);

						final FindReplaceDocumentAdapter frda = new FindReplaceDocumentAdapter(document);
						try {
							if (JAVA.equals(extension)) {
								IRegion region = frda.find(0, '"' + wicketId + '"', true, true, false, false);
								if (region != null) {
									while (region != null) {
										final IRegion li = document.getLineInformationOfOffset(region.getOffset());
										String line = document.get(li.getOffset(), li.getLength()).trim();
										if (line.startsWith("*") || line.startsWith("/*") || line.startsWith("//")) {
											region = frda.find(region.getOffset() + 1, '"' + wicketId + '"', true, true, false, false);
										} else {
											DocumentHelper.markOccurrence(textEditor, DocumentHelper.getStringConstantName(line));
											textEditor.selectAndReveal(region.getOffset() + 1, wicketId.length());
											found = true;
											break;
										}
									}
								} else {
									// wicket id not found in file, so search up in tree
									final List<Object> supertypes = TypeHelper.getSupertypes(file);
									if (supertypes.size() > 0) {
										if (supertypes.get(0) instanceof IFile) {
											final IEditorPart oe = IDE.openEditor(activePage, (IFile) supertypes.get(0), false);
											open();
											if (!found) {
												activePage.closeEditor(oe, false);
											}
										}
									}
								}
							} else if (HTML.equals(extension)) {
								String wid_const = DocumentHelper.getNamespacePrefix(document);
								final IRegion region = frda.find(0, wid_const + ":id=\"" + wicketId, true, true, true, false);
								if (region != null) {
									textEditor.selectAndReveal(region.getOffset() + wid_const.length() + 5, wicketId.length());
									break;
								}
							} else if (PROPERTIES.equals(extension)) {
								// for the wicket tags that use wicket:message
								if (wicketId.startsWith("value:")) {
									wicketId = wicketId.substring(6);
								}
								final IRegion regionBegin = frda.find(0, wicketId, true, true, false, false);
								if (regionBegin != null) {
									IRegion sr = frda.find(regionBegin.getOffset(), "\">", true, true, false, false);
									if (sr == null) { // properties, select till eol
										sr = frda.find(regionBegin.getOffset(), "=", true, true, false, false);
										if (sr == null) {
											activePage.closeEditor(editor, false);
											continue;
										}
										final IRegion lineRegion = document.getLineInformationOfOffset(sr.getOffset());
										final int selectionLength = lineRegion.getOffset() + lineRegion.getLength() - sr.getOffset();
										textEditor.selectAndReveal(sr.getOffset() + 1, selectionLength - 1);
										foundInPropertiesFile = true;
										break;
									} else { // xml, select till </
										final IRegion selEnd = frda.find(regionBegin.getOffset(), "</", true, true, false, false);
										textEditor.selectAndReveal(sr.getOffset() + 2, selEnd.getOffset() - sr.getOffset() - 2);
										foundInPropertiesFile = true;
										break;
									}
								} else {
									activePage.closeEditor(editor, false);
									continue;
								}
							}
						} catch (final BadLocationException e) {
							textEditor.resetHighlightRange();
							return;
						}
					} else {
						if (!found) {
							if (JAVA.equals(extension) && !openedResource.getName().contains("_")) {
								createJavaFile(openedResource);
							} else if (HTML.equals(extension)) {
								createHtmlFile(workbench);
							}
						}
					}
				} catch (final PartInitException pie) {
					return;
				}
			}
			if (PROPERTIES.equals(extension) && !foundInPropertiesFile) {
				if (createPropertiesFile(openedResource.getFullPath().removeFileExtension().addFileExtension(PROPERTIES).toPortableString(),
						wicketId + "=") != null) {
					open();
				}
			}
		}
	}

	private boolean samePackage(final String portableString, final String toOpenFilename) {
		String[] segmentsA = portableString.split("/");
		String[] segmentsB = toOpenFilename.split("/");
		if (segmentsA.length != segmentsB.length) {
			return false;
		}
		for (int i = segmentsA.length - 2; i >= 0; i--) {
			if (segmentsA[i].equals(segmentsB[i])) {
				continue;
			} else {
				return false;
			}
		}
		return true;
	}

	public List<String> getFilenamesToOpen(final IResource openedResource, final String extension) {
		Assert.isNotNull(openedResource);
		String ressourceExtension = openedResource.getFileExtension();

		final Set<String> filenames = new LinkedHashSet<String>();
		if (openJavaFileOnly) {
			filenames.add(getJavaFile(openedResource).getFullPath().toPortableString());
			return new ArrayList<String>(filenames);
		}
		if (HTML.equals(ressourceExtension)) {
			filenames.add(getJavaFile(openedResource).getFullPath().toPortableString());
			filenames.addAll(getPropertiesFilename(openedResource));
		} else if (JAVA.equals(ressourceExtension)) {
			if (!PROPERTIES.equals(extension)) {
				filenames.addAll(getHtmlFiles(openedResource));
			}
			filenames.addAll(getPropertiesFilename(openedResource));
		} else if (PROPERTIES.equals(ressourceExtension)) {
			filenames.addAll(getHtmlFiles(openedResource));
			filenames.add(getJavaFile(openedResource).getFullPath().toPortableString());
		}
		return new ArrayList<String>(filenames);
	}

	public boolean openJavaFile() {
		return JAVA.equals(extension);
	}

	public void openJavaFileOnly(final boolean openJavaFileOnly) {
		this.openJavaFileOnly = openJavaFileOnly;
	}

	public static List<String> getHtmlFiles(final IResource openedResource) {
		Assert.isNotNull(openedResource);
		final IProject project = openedResource.getProject();
		List<String> htmlFilenames = new ArrayList<String>();
		final String filename = openedResource.getFullPath().removeFileExtension().addFileExtension(HTML).toPortableString();

		final IFile file = getFile(filename);
		// is there a html file in the same folder?
		if (file != null && file.exists()) {
			htmlFilenames.add(filename);
		} else { // if not, search for one with the same name
			final FileSearcher fs = new FileSearcher(project, new Path(filename).lastSegment());
			try {
				final IJavaProject javaProject = (IJavaProject) project.getNature(JavaCore.NATURE_ID);
				final IPackageFragmentRoot[] packageFragmentRoots = javaProject.getPackageFragmentRoots();
				project.accept(fs);
				for (final IFile foundFile : fs.getFoundFiles()) {
					for (final IPackageFragmentRoot packageFragmentRoot : packageFragmentRoots) {
						if (packageFragmentRoot.getKind() == 1) { // if it's in a source folder
							if (packageFragmentRoot.getPath().segment(1).equals(foundFile.getFullPath().segment(1))) { // starting with /src
								htmlFilenames.add(foundFile.getFullPath().toPortableString());
							}
						}
					}
				}
			} catch (final CoreException e1) {
			}
		}
		FileSearcher fs = new FileSearcher(project, new Path(filename).removeFileExtension().lastSegment() + "$*");
		try {
			project.accept(fs);
			for (final IFile foundFile : fs.getFoundFiles()) {
				htmlFilenames.add(foundFile.getFullPath().toPortableString());
			}
		} catch (CoreException e) {
		}

		Collections.reverse(htmlFilenames);
		return htmlFilenames;
	}

	public static IFile getJavaFile(final IResource openedResource) {
		Assert.isNotNull(openedResource);
		String filePath = openedResource.getFullPath().removeFileExtension().addFileExtension(JAVA).toPortableString();
		String searchName = new Path(filePath).lastSegment();
		if (searchName.contains("_")) {
			searchName = searchName.split("_")[0].concat(".java");
		}
		// it is an inner class
		if (searchName.contains("$")) {
			searchName = searchName.split("\\$")[0].concat(".java");
		}
		filePath = openedResource.getFullPath().removeLastSegments(1).append(searchName).toPortableString();

		final IFile file = getFile(filePath);
		// is there a file with the same name in the same folder?
		if (file != null && file.exists()) {
			return file;
		} else { // if not, search for one with the same name, but without variations
			// String searchName = new Path(filename).lastSegment();
			// if (searchName.contains("_")) {
			// searchName = searchName.split("_")[0].concat(".java");
			// }
			// it is an inner class
			// if (searchName.contains("$")) {
			// searchName = searchName.split("\\$")[0].concat(".java");
			// }
			final FileSearcher fs = new FileSearcher(openedResource.getProject(), searchName);
			try {
				final IProject project = openedResource.getProject();
				project.accept(fs);

				if (fs.getFoundFiles().size() > 1) { // more then one file found means, there are other packages with the same filenames
					List<IPath> sourceRoots = FileSearcher.getSourceFolders(project);
					IPath orp = openedResource.getFullPath().removeLastSegments(1);
					for (IPath sourceRoot : sourceRoots) {
						if (sourceRoot.isPrefixOf(orp)) {
							for (IFile ff : fs.getFoundFiles()) {
								final IPath ffr = ff.getFullPath().removeFirstSegments(sourceRoot.segmentCount()).removeLastSegments(1);
								orp = openedResource.getFullPath().removeFirstSegments(sourceRoot.segmentCount()).removeLastSegments(1);
								if (orp.toPortableString().equals(ffr.toPortableString())) {
									return ff;
								}
							}
						}
					}
				} else if (fs.getFoundFile() != null) {
					return fs.getFoundFile();
				}
			} catch (final CoreException e1) {
			}
		}

		return file;
	}

	/** Properties can be properties files but also xml files */
	public static List<String> getPropertiesFilename(final IResource openedResource) {
		Assert.isNotNull(openedResource);

		final List<String> propertyFiles = getWicketFile(openedResource, PROPERTIES);
		propertyFiles.add(openedResource.getParent().getFullPath() + "/wicket-package.properties");
		final List<String> xmlPropertyFiles = getWicketFile(openedResource, XML_PROPERTIES);
		propertyFiles.addAll(xmlPropertyFiles);
		final List<String> xmlFiles = getWicketFile(openedResource, XML);
		propertyFiles.addAll(xmlFiles);

		if (xmlFiles.size() == 1 || xmlPropertyFiles.size() == 1) {
			Collections.reverse(propertyFiles);
		}

		return propertyFiles;
	}

	private static List<String> getWicketFile(final IResource openedResource, final String ext) {
		Assert.isNotNull(openedResource);
		Assert.isNotNull(ext);

		final String filename = openedResource.getFullPath().removeFileExtension().addFileExtension(ext).toPortableString();
		final List<String> filenames = new ArrayList<String>();
		final IFile file = getFile(filename);

		// is there a file in the same folder, with the same name? then take this one first
		if (file != null && file.exists()) {
			filenames.add(filename);
		} else { // if not, search for one with the same name
			final FileSearcher fs = new FileSearcher(openedResource.getProject(),
					openedResource.getFullPath().removeFileExtension().lastSegment() + "*." + ext);
			try {
				final IProject project = openedResource.getProject();
				project.accept(fs);
				final List<IFile> ffs = fs.getFoundFiles();
				if (ffs != null) {
					for (final IFile ff : ffs) {
						filenames.add(ff.getFullPath().toPortableString());
					}
				}
			} catch (final CoreException e1) {
			}
		}
		return filenames;
	}

	private void createHtmlFile(final IWorkbench workbench) {
		/*
		 * Do nothing, the html creating wizard in eclipse is not very mature
		 * final NewHTMLWizard wiz = new NewHTMLWizard(); wiz.init(workbench,
		 * new StructuredSelection()); final WizardDialog dialog = new
		 * WizardDialog(workbench.getActiveWorkbenchWindow().getShell(), wiz);
		 * dialog.create(); dialog.open();
		 */
	}

	private IFile createPropertiesFile(final String toOpenFilename, final String key) {
		final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(toOpenFilename));
		try {
			if (!file.exists()) {
				final InputStream ins = new ByteArrayInputStream(key.getBytes());
				file.create(ins, true, null);
			} else {
				final InputStream ins = new ByteArrayInputStream(("\n" + key).getBytes());
				file.appendContents(ins, IFile.FORCE | IFile.KEEP_HISTORY, null);
			}
		} catch (final CoreException e) {
		}
		return file;
	}

	private void createJavaFile(final IResource resource) {
		Assert.isNotNull(resource);

		final OpenNewClassWizardAction action = new OpenNewClassWizardAction();
		final NewClassWizardPage ncwp = new NewClassWizardPage();

		ncwp.setTypeName(resource.getName().replaceAll("\\.html", ""), true);

		final IJavaProject javaProject = JavaCore.create(resource.getProject());
		IPackageFragmentRoot root = null;
		try {
			final IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
			for (int i = 0; i < roots.length; i++) {
				if (roots[i].getKind() == IPackageFragmentRoot.K_SOURCE) {
					root = roots[i];
					break;
				}
			}

			ncwp.setPackageFragmentRoot(root, true);

		} catch (final JavaModelException e) {
		}
		final String os = root.getParent().getPath().toPortableString();
		final String fp = root.getResource().getFullPath().toPortableString().replaceFirst(os, "").substring(1);
		final String ps = resource.getProjectRelativePath().toPortableString().replaceFirst(fp, "");
		String pn = ps.replaceFirst(resource.getName(), "").substring(1).replaceAll("/", ".");
		pn = pn.substring(0, pn.length() - 1);

		ncwp.setPackageFragment(root.getPackageFragment(pn), true);

		ncwp.setSuperClass("org.apache.wicket.markup.html.WebPage", openJavaFile());

		action.setConfiguredWizardPage(ncwp);
		action.setOpenEditorOnFinish(true);
		action.run();
	}

	/** Helper to get a IFile in this workspace */
	public static IFile getFile(final String filename) {
		IFile file = null;

		if (filename != null) {
			final IPath filePath = new Path(filename);
			if ((filePath.segmentCount() > 1) && (ResourcesPlugin.getWorkspace().getRoot().getFile(filePath).exists())) {
				return ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
			}
			final IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(URIUtil.toURI(filePath.makeAbsolute()));
			for (int i = 0; (i < files.length) && (file == null); ++i) {
				if (files[i].exists()) {
					file = files[i];
					break;
				}
			}
		}

		return file;
	}

	public String getTypeLabel() {
		return null;
	}

	public String getHyperlinkText() {
		return "Open " + extension + " file and jump to " + QWickieActivator.WICKET_ID + " \"" + wicketId + "\"";
	}

	public String getURLString() {
		return this.wicketId;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " for " + QWickieActivator.WICKET_ID + " \"" + wicketId + "\" to " + extension + " file";
	}

}