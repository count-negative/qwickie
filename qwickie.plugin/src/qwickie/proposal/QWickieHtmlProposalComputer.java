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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer;

import qwickie.hyperlink.WicketHyperlink;
import qwickie.util.DocumentHelper;

public class QWickieHtmlProposalComputer implements ICompletionProposalComputer {
	final Image img = new Image(Display.getCurrent(), QWickieProposalComputer.class.getResourceAsStream("/qwickie.png"));

	public List<CompletionProposal> computeCompletionProposals(final CompletionProposalInvocationContext context, final IProgressMonitor monitor) {
		// used a Map because CompletionProposal doesn't implement hashcode/equals
		Map<String, CompletionProposal> proposals = new LinkedHashMap<String, CompletionProposal>();

		final IDocument document = context.getDocument();
		final String wicketNS = DocumentHelper.getNamespacePrefix(document);
		final IRegion rtr = DocumentHelper.findStringArgumentInMarkup(document, context.getInvocationOffset(), wicketNS + ":id");
		if (rtr == null) {
			return new ArrayList<CompletionProposal>();
		}
		try {
			final String existingWid = document.get(rtr.getOffset(), rtr.getLength());
			final IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
			if (activeEditor != null) { // If it's null then there is no editor connected to the java file extension
				final IEditorInput editorInput = activeEditor.getEditorInput();
				final IFile htmlFile = ResourceUtil.getFile(editorInput);
				final IFile javaFile = WicketHyperlink.getJavaFile(htmlFile);
				final IDocumentProvider provider = new TextFileDocumentProvider();
				provider.connect(javaFile);

				final IDocument jdoc = provider.getDocument(javaFile);
				int nol = jdoc.getNumberOfLines();
				for (int lidx = 0; lidx < nol; lidx++) {
					IRegion li = jdoc.getLineInformation(lidx);
					String line = jdoc.get(li.getOffset(), li.getLength());
					String[] ss = line.split("\"");
					if (ss.length > 1) {
						for (int i = 1; i < ss.length; i += 2) {
							final String proposedWid = ss[i];
							if (proposedWid.length() > 0 && !proposedWid.contains(":") && !proposedWid.contains("~")) {
								final CompletionProposal proposal = new CompletionProposal(proposedWid, rtr.getOffset(), existingWid.length(), rtr.getOffset(),
										img, proposedWid, null, line.replaceAll("\"" + proposedWid + "\"", "\"<b>" + proposedWid + "</b>\""));
								proposals.put(proposedWid, proposal);
							}
						}
					}
				}
				provider.disconnect(javaFile);
			}
		} catch (Exception e) {
		}

		return new ArrayList<CompletionProposal>(proposals.values());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer#
	 * computeContextInformation(org.eclipse.wst.sse.ui.contentassist.
	 * CompletionProposalInvocationContext,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public List<?> computeContextInformation(final CompletionProposalInvocationContext arg0, final IProgressMonitor arg1) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer#
	 * getErrorMessage()
	 */
	public String getErrorMessage() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer#
	 * sessionEnded()
	 */
	public void sessionEnded() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.wst.sse.ui.contentassist.ICompletionProposalComputer#
	 * sessionStarted()
	 */
	public void sessionStarted() {
	}
}
