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
package qwickie.proposal;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import qwickie.QWickieActivator;
import qwickie.hyperlink.WicketHyperlink;
import qwickie.util.DocumentHelper;
import qwickie.util.TypeHelper;

/**
 * @author count.negative
 *
 */
public class QWickieProposalComputer implements IJavaCompletionProposalComputer {

	private static final Image img = new Image(Display.getCurrent(), QWickieProposalComputer.class.getResourceAsStream("/qwickie.png"));
	private Map<String, CompletionProposal> proposals;

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jdt.internal.ui.text.java.JavaCompletionProposalComputer#
	 * computeCompletionProposals(org.eclipse.jdt.ui.text.java.
	 * ContentAssistInvocationContext ,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public List<ICompletionProposal> computeCompletionProposals(final ContentAssistInvocationContext context, final IProgressMonitor monitor) {
		final boolean wasEmpty = proposals.isEmpty();

		if (context instanceof JavaContentAssistInvocationContext) {
			final JavaContentAssistInvocationContext jcaic = (JavaContentAssistInvocationContext) context;
			final IType expectedType = jcaic.getExpectedType();
			if (expectedType != null && "String".equals(expectedType.getElementName())) {

				final IDocument document = jcaic.getDocument();
				final ICompilationUnit cu = jcaic.getCompilationUnit();

				final IRegion wcr = DocumentHelper.findWicketComponentRegion(document, context.getInvocationOffset());
				if (wcr != null) {
					final IRegion javaRegion = DocumentHelper.findWord(document, wcr.getOffset());
					try {
						final IJavaElement[] jes = cu.codeSelect(javaRegion.getOffset(), javaRegion.getLength());
						if (jes.length == 1 && TypeHelper.isWicketJavaElement(jes[0])) {

							final IResource resource = cu.getResource();
							final String openKind = jes[0].getElementName().equals(DocumentHelper.GET_STRING) ? WicketHyperlink.PROPERTIES
									: WicketHyperlink.HTML;
							final WicketHyperlink wh = new WicketHyperlink(new Region(0, 0), "", openKind);
							final List<String> filenamesToOpen = wh.getFilenamesToOpen(resource, openKind);

							for (final String filename : filenamesToOpen) {
								final IFile file = WicketHyperlink.getFile(filename);
								try {
									final InputStream contents = file.getContents();
									final BufferedReader br = new BufferedReader(new InputStreamReader(contents));
									String line = null;
									boolean commented = false;
									while ((line = br.readLine()) != null) {

										if (openKind.equals(WicketHyperlink.HTML)) {
											if (line.contains("<!--")) {
												commented = true;
												continue;
											}
											if (line.contains("-->")) {
												commented = false;
												continue;
											}

											if (line.contains(QWickieActivator.WICKET_ID + "=\"") && !commented) {
												final String[] wids = getWicketIdsFrom(line);
												for (final String wid : wids) {
													final String additionalInfo = line.trim().replaceAll("<", "&lt;").replaceAll("\"" + wid + "\"",
															"\"<b>" + wid + "</b>\"");
													final IRegion sar = DocumentHelper.findStringArgumentInJava(document, context.getInvocationOffset());

													final CompletionProposal ccp = new CompletionProposal(wid, sar.getOffset(), sar.getLength(), 0, img, wid,
															null, additionalInfo);
													proposals.put(wid, ccp);
												}
											}
										} else if (openKind.equals(WicketHyperlink.PROPERTIES)) {

											if (filename.endsWith(WicketHyperlink.PROPERTIES) && line.contains("=")) {

												final String key = line.split("=")[0];
												final IRegion sar = DocumentHelper.findStringArgumentInJava(document, context.getInvocationOffset());
												final CompletionProposal ccp = new CompletionProposal(key, sar.getOffset(), sar.getLength(), 0, img, key, null,
														line.trim().replaceAll(key, "<b>" + key + "</b>"));
												proposals.put(key, ccp);
											} else if (filename.endsWith(WicketHyperlink.XML) && line.contains("<entry key=\"")) {

												final String key = line.split("<entry key=\"")[1].split("\">")[0];
												final IRegion sar = DocumentHelper.findStringArgumentInJava(document, context.getInvocationOffset());
												final CompletionProposal ccp = new CompletionProposal(key, sar.getOffset(), sar.getLength(), 0, img, key, null,
														line.trim().replaceAll("<", "&lt;").replaceAll("\"" + key + "\"", "\"<b>" + key + "</b>\""));
												proposals.put(key, ccp);
											}
										}
									}
									br.close();
									contents.close();
								} catch (final Exception e) {
								}
							}
						}
					} catch (final Exception e) {
					}

					// if the proposals where empty, then show only the unused ids
					// but if ctrl+space was pressed twice, then show all ids on the page
					if (wasEmpty) {
						removeExistingIds(proposals, document.get());
					}
				}
			}

		}

		return new ArrayList<ICompletionProposal>(proposals.values());
	}

	private void removeExistingIds(final Map<String, CompletionProposal> proposals, final String text) {
		if (!"".equals(text)) {
			final String[] ids = text.split("\"");
			if (ids.length > 1) {
				for (int i = 0; i < ids.length; i++) {
					final String id = ids[i];
					proposals.remove(id);
				}
			}
		}
	}

	/** get all the wicket:ids in a line of HTML */
	private String[] getWicketIdsFrom(final String line) {
		final String[] ss = line.split(QWickieActivator.WICKET_ID + "=\"");
		final String wids[] = new String[ss.length - 1];
		for (int i = 1; i < ss.length; i++) {
			wids[i - 1] = ss[i].split("\"")[0];
		}
		return wids;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer#
	 * sessionStarted()
	 */
	@Override
	public void sessionStarted() {
		proposals = new HashMap<String, CompletionProposal>();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer#
	 * computeContextInformation(org.eclipse.jdt.ui.text.java.
	 * ContentAssistInvocationContext,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public List<IContextInformation> computeContextInformation(final ContentAssistInvocationContext paramContentAssistInvocationContext,
			final IProgressMonitor paramIProgressMonitor) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer#
	 * getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer#sessionEnded
	 * ()
	 */
	@Override
	public void sessionEnded() {
		proposals = null;
	}

}
