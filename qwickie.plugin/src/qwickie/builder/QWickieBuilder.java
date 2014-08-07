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
package qwickie.builder;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.ResolvedBinaryType;
import org.eclipse.jdt.internal.core.ResolvedSourceType;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;

import qwickie.QWickieActivator;
import qwickie.hyperlink.WicketHyperlink;
import qwickie.preferences.QWickiePreferencePage;
import qwickie.util.DocumentHelper;
import qwickie.util.FileSearcher;
import qwickie.util.TypeHelper;

@SuppressWarnings("restriction")
public class QWickieBuilder extends IncrementalProjectBuilder {

	private final IPreferenceStore store = QWickieActivator.getDefault().getPreferenceStore();
	private String[] excludes = new String[] {};

	private static int LDL = System.getProperty("line.separator").length();
	public static final String BUILDER_ID = "qwickie.qwickieBuilder";
	public static final String MARKER_TYPE = "qwickie.qwickieProblem";
	public static final String MARKER_ATTRIB_WICKET_ID = "wicketId";
	public static final String MARKER_ATTRIB_HTML_SNIPPET = "htmlSnippet";
	private static final String JAVA_EXT = "java";
	private static final String HTML_EXT = "html";
	private List<IPath> srcFolders = new ArrayList<IPath>();
	private int severity = 2;

	class QWickieDeltaVisitor implements IResourceDeltaVisitor {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		public boolean visit(final IResourceDelta delta) throws CoreException {
			final IResource resource = delta.getResource();
			if (resource == null) {
				return true;
			}

			switch (delta.getKind()) {
				case IResourceDelta.ADDED:
					// handle added resource
					checkWicketIds(resource);
					break;
				case IResourceDelta.REMOVED:
					// handle removed resource
					break;
				case IResourceDelta.CHANGED:
					// handle changed resource
					checkWicketIds(resource);
					break;
			}
			// return true to continue visiting children.
			return true;
		}
	}

	class QWickieResourceVisitor implements IResourceVisitor {
		public boolean visit(final IResource resource) {
			checkWicketIds(resource);
			// return true to continue visiting children.
			return true;
		}
	}

	private void addMarker(final IFile file, final String message, final String wid, final String htmlSnippet, int lineNumber, final int startChar, final int length) {
		try {
			final IMarker marker = file.createMarker(MARKER_TYPE);
			marker.setAttribute(MARKER_ATTRIB_WICKET_ID, wid);
			marker.setAttribute(MARKER_ATTRIB_HTML_SNIPPET, htmlSnippet);
			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
			marker.setAttribute(IMarker.SEVERITY, severity);
			if (lineNumber == -1) {
				lineNumber = 1;
			}
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			marker.setAttribute(IMarker.PROBLEM, true);
			marker.setAttribute(IMarker.CHAR_START, startChar);
			marker.setAttribute(IMarker.CHAR_END, startChar + length);
		} catch (final CoreException e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int, java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IProject[] build(final int kind, final Map<String, String> args, final IProgressMonitor monitor) throws CoreException {
		srcFolders = FileSearcher.getSourceFolders(getProject());
		excludes = store.getString("excludes").split(",");
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			final IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	void checkWicketIds(final IResource resource) {
		if (resource == null || !(resource instanceof IFile) || isExcluded((IFile) resource)) {
			return;
		}
		if (srcFolders.isEmpty()) {
			srcFolders = FileSearcher.getSourceFolders(getProject());
		}
		if (excludes.length == 0) {
			excludes = store.getString("excludes").split(",");
		}

		final String fileExtension = resource.getFileExtension();
		// check html files, that are in the source folders
		if (HTML_EXT.equals(fileExtension)) {
			final IFile htmlFile = (IFile) resource;
			final IFile javaFile = WicketHyperlink.getJavaFile(resource);
			deleteMarkers(htmlFile);
			deleteMarkers(javaFile);
			if (isExcluded(javaFile)) {
				return;
			}

			try {
				final IDocumentProvider provider = new TextFileDocumentProvider();
				provider.connect(htmlFile);
				final IDocument document = provider.getDocument(htmlFile);
				LDL = document.getLineDelimiter(0).length();
				int sc = 0;
				boolean commented = false;
				String wid_const = DocumentHelper.WICKET;
				for (int i = 0; i < document.getNumberOfLines(); i++) {
					final IRegion li = document.getLineInformation(i);
					final String line = document.get(li.getOffset(), li.getLength());

					if (line.contains("<!--")) {
						commented = true;
						continue;
					}
					if (line.contains("-->")) {
						commented = false;
						continue;
					}

					if (line.contains(QWickieActivator.WICKET_DTD)) {
						wid_const = DocumentHelper.getWicketNamespace(line);
					}

					final String wid_const_id = wid_const + ":id=\"";
					if (line.contains(wid_const_id) && !commented) {
						final String wid = line.split(wid_const_id)[1].split("\"")[0];
						int jlc = getJavaLine(javaFile, wid);
						if (jlc == -1) { // wicket id was not found in this java file
							// get the supertypes for the WebPage (maybe it's added in a BasePage?)
							final List<Object> superTypes = TypeHelper.getSupertypes(javaFile);
							for (final Object superType : superTypes) {
								if (superType instanceof IFile) {
									jlc = getJavaLine((IFile) superType, wid);
								} else if (superType instanceof IClassFile) {
									jlc = getJavaLine((IClassFile) superType, wid);
								}
								if (jlc == -1) {
									// not found, then search in the used types
									final List<JavaElement> wcts = TypeHelper.getWicketComponentTypes(javaFile);
									for (final JavaElement wct : wcts) {
										jlc = getJavaLine(wct, wid);
										if (jlc > -1) {
											break;
										}
									}
									if (jlc == -1) {
										// not found, then search in the used models
										final List<IVariableBinding> wmts = TypeHelper.getWicketModelTypes(javaFile);
										for (final IVariableBinding wmt : wmts) {
											if (wid.equals(wmt.getName())) {
												jlc = 0;
												break;
											}
										}
									}
								}
								if (jlc > -1) { // found, so don't go further
									break;
								}
							}
							if (jlc == -1) {
								final int widPos = sc + line.indexOf("\"" + wid + "\"") + 1;
								FindReplaceDocumentAdapter frda = new FindReplaceDocumentAdapter(document);
								IRegion tagBegin = frda.find(widPos, "<", false, true, false, false);
								IRegion tagEnd = frda.find(tagBegin.getOffset(), " ", true, true, false, false);
								String htmlTag = document.get(tagBegin.getOffset() + 1, tagEnd.getOffset() - tagBegin.getOffset() - 1);
								String htmlSnippet = htmlTag.toLowerCase();
								if (htmlFile != null && "input".equals(htmlSnippet)) {
									tagEnd = frda.find(tagBegin.getOffset(), ">", true, true, false, false);
									htmlSnippet = document.get(tagBegin.getOffset() + 1, tagEnd.getOffset() - tagBegin.getOffset() - 1).toLowerCase().trim();
									if (htmlSnippet.contains("type")) {
										final String[] hss = htmlSnippet.split(" ");
										if (hss != null) {
											for (int j = 0; j < hss.length; j++) {
												String hs = hss[j];
												if (hs.startsWith("type")) {
													htmlSnippet = htmlTag + " " + hs.replace("type=", "").split("\"")[1];
												}
											}
										}
									} else {
										htmlSnippet = htmlTag.toLowerCase();
									}
								}
								addMarker(htmlFile, getErrorText(wid), wid, htmlSnippet, i, widPos, wid.length());
								markJava(javaFile, wid, htmlSnippet);
							}
						}
					}
					sc += li.getLength() + LDL;
				}
				provider.disconnect(htmlFile);
			} catch (final Exception e) {
				QWickieActivator.getDefault().getLog().log(new Status(Status.ERROR, QWickieActivator.PLUGIN_ID, "Error in builder", e));
			}
		}

		// check java files
		if (JAVA_EXT.equals(fileExtension)) {
			final List<String> htmlFilenames = WicketHyperlink.getHtmlFiles(resource);
			for (String htmlFilename : htmlFilenames) {
				final IFile htmlFile = WicketHyperlink.getFile(htmlFilename);
				checkWicketIds(htmlFile);
			}
		}
	}

	/**
	 * Is this file excluded from beeing checked
	 */
	private boolean isExcluded(final IFile file) {
		for (String exclude : excludes) {
			if (exclude.length() > 0 && file != null && file.getProjectRelativePath().toString().startsWith(exclude)) {
				return true;
			}
		}
		return !isSource(file);
	}

	/**
	 * mark an error in a java file
	 */
	private void markJava(final IFile javaFile, final String wid, final String htmlSnippet) {
		final IDocumentProvider provider = new TextFileDocumentProvider();
		try {
			provider.connect(javaFile);
			final IDocument document = provider.getDocument(javaFile);
			final String className = javaFile.getName().replaceAll(".java", "");
			final FindReplaceDocumentAdapter frda = new FindReplaceDocumentAdapter(document);
			final IRegion region = frda.find(0, "class " + className, true, true, true, false);
			if (region != null) {
				addMarker(javaFile, getErrorText(wid), wid, htmlSnippet, document.getLineOfOffset(region.getOffset()), region.getOffset() + 6, className.length());
			}
			provider.disconnect(javaFile);
		} catch (final Exception e) {
		}
	}

	/** get the error text that is displayed */
	private String getErrorText(final String wid) {
		return QWickieActivator.WICKET_ID + " \"" + wid + "\" not found in java file(s)";
	}

	/** search for the line, containing the wicket:id in a java file */
	private int getJavaLine(final IFile javaFile, final String wid) {
		try {
			final InputStream contents = javaFile.getContents(true);
			final BufferedReader br = new BufferedReader(new InputStreamReader(contents));
			final char[] c = new char[contents.available()];
			br.read(c);
			br.close();
			contents.close();
			return getJavaLine(new String(c), wid);
		} catch (final Exception e) {
		}
		return -1;
	}

	private int getJavaLine(final JavaElement je, final String wid) {
		Assert.isNotNull(je);
		try {
			if (je instanceof ResolvedBinaryType) {
				ResolvedBinaryType rbt = (ResolvedBinaryType) je;
				String source = rbt.getSource(); // try to get the source
				// shorten search for know wicket:ids
				if ((je.getElementName().equals("FilterToolbar") && ("focus-tracker".equals(wid) || "focus-restore".equals(wid))) || ("cols".equals(wid) && je.getElementName().equals("GridView"))) {
					return 1;
				}
				return getJavaLine(source, wid);
			}
			if (je instanceof ResolvedSourceType) {
				return getJavaLine(((ResolvedSourceType) je).getSource(), wid);
			}
			return getJavaLine(je.getClassFile().getSource(), wid);
		} catch (final JavaModelException e) {
		}
		return -1;
	}

	/** search for the line, containing the wicket:id in a java file */
	private int getJavaLine(final IClassFile classFile, final String wid) {
		Assert.isNotNull(classFile);
		try {
			return getJavaLine(classFile.getSource(), wid);
		} catch (final JavaModelException e) {
		}
		return -1;
	}

	private boolean lineContains(final String line, final String wid) {
		return line.contains("\"" + wid + "\"");
	}

	private int getJavaLine(final String source, final String wid) {
		int lc = -1;
		if (source == null) {
			return -1;
		}
		final String[] lines = source.split("\n");
		boolean commented = false;
		for (final String line : lines) {
			lc++;

			// the complete line is a comment, so go away
			if (line.trim().startsWith("//")) {
				continue;
			}
			if (line.contains("/*")) { // a comment is starting
				commented = true;
			}
			if (line.contains("*/")) { // comment ends
				commented = false;
			}

			if (lineContains(line, wid) && !commented) {
				return lc;
			}
		}
		return -1;
	}

	/** check if resource is in an source folder */
	private boolean isSource(final IResource resource) {
		if (resource == null || resource.getFullPath() == null) {
			return false;
		}
		final String ps = resource.getFullPath().toPortableString();
		for (final IPath srcFolder : srcFolders) {
			if (ps.startsWith(srcFolder.toPortableString())) {
				return true;
			}
		}
		return false;
	}

	private void deleteMarkers(final IFile file) {
		if (file == null) {
			return;
		}
		try {
			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
		} catch (final CoreException ce) {
		}
	}

	public void fullBuild(final IProgressMonitor monitor) throws CoreException {
		try {
			final String ssever = store.getString(QWickiePreferencePage.SEVERITY);
			try {
				severity = Integer.parseInt(ssever);
			} catch (NumberFormatException nfe) {
				severity = QWickiePreferencePage.SEVERITIES.valueOf(ssever).ordinal();
			}
			getProject().accept(new QWickieResourceVisitor());
		} catch (final CoreException e) {
		}
	}

	protected void incrementalBuild(final IResourceDelta delta, final IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		String ssever = store.getString(QWickiePreferencePage.SEVERITY);
		if (ssever == null || "".equals(ssever)) {
			ssever=store.getDefaultString(QWickiePreferencePage.SEVERITY);
		}
		try {
			severity = Integer.parseInt(ssever);
		} catch (NumberFormatException nfe) {
			severity = QWickiePreferencePage.SEVERITIES.valueOf(ssever).ordinal();
		}
		delta.accept(new QWickieDeltaVisitor());
	}

}
