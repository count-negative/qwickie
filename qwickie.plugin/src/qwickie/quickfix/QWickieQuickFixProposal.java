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
package qwickie.quickfix;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import qwickie.QWickieActivator;
import qwickie.builder.QWickieBuilder;
import qwickie.preferences.QWickiePreferencePage;

public class QWickieQuickFixProposal implements IJavaCompletionProposal {
	private static final Image img = new Image(Display.getCurrent(), QWickieQuickFixProcessor.class.getResourceAsStream("/qwickie.png"));
	private static Map<String, String> codeTemplate = new HashMap<String, String>() {
		private static final long serialVersionUID = 1L;
		{
			put("a", "Link");
			put("div", "Label");
			put("span", "Label");
			put("p", "Label");
			put("img", "Image");
			put("form", "Form");
			put("table", "RepeatingView");
			put("submit", "SubmitLink");
			put("input", "TextField");
			put("input text", "TextField");
			put("input checkbox", "CheckBox");
			put("input radio", "Radio");
			put("input image", "ImageButton");
			put("input password", "PasswordTextField");
			put("input hidden", "HiddenField");
			put("input file", "FileUploadField");
			put("input submit", "Button");
			put("select", "DropDownChoice");
			put("textarea", "TextArea");
		}
	};

	private final IMarker marker;
	private final String wicketId;
	private final String htmlSnippet;
	private final IPreferenceStore store = QWickieActivator.getDefault().getPreferenceStore();

	public QWickieQuickFixProposal(final int selectionOffset, final int selectionLength, final IMarker marker) throws CoreException {
		this.marker = marker;
		this.wicketId = marker != null ? (String) marker.getAttribute(QWickieBuilder.MARKER_ATTRIB_WICKET_ID) : "";
		this.htmlSnippet = marker != null ? (String) marker.getAttribute(QWickieBuilder.MARKER_ATTRIB_HTML_SNIPPET) : "";
	}

	@Override
	public void apply(final IDocument document) {
		try {
			if (marker != null) {
				final FindReplaceDocumentAdapter frda = new FindReplaceDocumentAdapter(document);
				final IRegion initMethodRegion = getMethodName().startsWith("constructor") ? findCTOR(frda) : findOnInitialize(frda);
				final IRegion superRegion = findSuper(frda, initMethodRegion);
				final String lineDelimiter = document.getLineDelimiter(document.getLineOfOffset(initMethodRegion.getOffset()));
				final String template = codeTemplate.get(htmlSnippet);
				if (template == null) {
					return;
				}
				final String replace = (superRegion != null ? ";" : "{") + lineDelimiter + "add(new " + template + "(\"" + wicketId + "\"));";
				frda.replace(replace, false);
				marker.delete();
			}
		} catch (final Exception e) {
		}
	}

	private IRegion findSuper(final FindReplaceDocumentAdapter frda, final IRegion initMethodRegion) throws BadLocationException {
		final String onInitSuper = "super.onInitialize\\(";
		final String ctorSuper = "super\\(";
		final IRegion superRegion = frda.find(initMethodRegion.getOffset(), getMethodName().startsWith("constructor") ? ctorSuper : onInitSuper, true, true, false,
				true);
		frda.find(superRegion != null ? superRegion.getOffset() : initMethodRegion.getOffset(), superRegion != null ? ";" : "{", true, true, false, false);
		return superRegion;
	}

	private IRegion findOnInitialize(final FindReplaceDocumentAdapter frda) throws BadLocationException {
		final IRegion initMethodRegion = frda.find(0, "protected void onInitialize()", true, true, false, false);
		if (initMethodRegion == null) { // there is no onInitialize() method
			MessageDialog.openWarning(null, "onInitialize() missing", "no method \"protected void onInitialize()\" found. Please create it first.");
		}
		return initMethodRegion;
	}

	private IRegion findCTOR(final FindReplaceDocumentAdapter frda) throws BadLocationException {
		IRegion initMethodRegion = frda.find(0, marker.getResource().getFullPath().removeFileExtension().lastSegment(), true, true, true, false);
		initMethodRegion = frda.find(initMethodRegion.getOffset() + 1, marker.getResource().getFullPath().removeFileExtension().lastSegment(), true, true, true,
				false);
		if (initMethodRegion == null) { // there is no CTOR
			MessageDialog.openWarning(null, "constructor missing", "no method constructor found. Please create one first.");
		}
		return initMethodRegion;
	}

	@Override
	public Point getSelection(final IDocument document) {
		return null;
	}

	@Override
	public String getAdditionalProposalInfo() {
		final String template = codeTemplate.get(htmlSnippet);
		if (template == null) {
			return null;
		}
		return (marker == null ? "add all the missing wicket components to the " + getMethodName()
		: "adds a <b>new " + template + "(\"" + wicketId + "\");</b> to the " + getMethodName());
	}

	@Override
	public String getDisplayString() {
		final String template = codeTemplate.get(htmlSnippet);
		if (template == null) {
			return null;
		}
		return (marker == null ? "add missing wicket components" : "add new " + template + "(\"" + wicketId + "\")");
	}

	@Override
	public Image getImage() {
		return img;
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

	@Override
	public int getRelevance() {
		return 0;
	}

	private String getMethodName() {
		if ("init".equals(store.getString(QWickiePreferencePage.ADD_NEW_COMPONENTS))) {
			return "onInitialize method";
		} else {
			return "constructor";
		}
	}

}
