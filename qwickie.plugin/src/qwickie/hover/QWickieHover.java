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
package qwickie.hover;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.internal.corext.util.Strings;
import org.eclipse.jdt.ui.text.java.hover.IJavaEditorTextHover;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;

import qwickie.QWickieActivator;
import qwickie.hyperlink.WicketHyperlink;
import qwickie.util.DocumentHelper;

/**
 * Hover shows the html line for this wicket:id
 * 
 * @author count.negative
 * 
 */
@SuppressWarnings("restriction")
public class QWickieHover implements IJavaEditorTextHover {
	private IEditorPart editor;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jdt.ui.text.java.hover.IJavaEditorTextHover#setEditor(org
	 * .eclipse.ui.IEditorPart)
	 */
	public void setEditor(final IEditorPart paramIEditorPart) {
		editor = paramIEditorPart;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text
	 * .ITextViewer, org.eclipse.jface.text.IRegion)
	 */
	public String getHoverInfo(final ITextViewer textViewer, final IRegion region) {
		Assert.isNotNull(textViewer);
		Assert.isNotNull(region);

		String text = null;
		final IDocument document = textViewer.getDocument();
		final IRegion widRegion = DocumentHelper.findStringArgumentInJava(document, region.getOffset());
		if (widRegion == null) {
			return null;
		}
		String wicketId = null;
		try {
			wicketId = document.get(widRegion.getOffset(), widRegion.getLength());
		} catch (final BadLocationException e1) {
		}

		final IResource openedResource = (IResource) editor.getEditorInput().getAdapter(IResource.class);
		if (openedResource == null) {
			return text;
		}
		final List<String> toOpenFilenames = WicketHyperlink.getHtmlFiles(openedResource);
		for (String toOpenFilename : toOpenFilenames) {

			final IFile file = WicketHyperlink.getFile(toOpenFilename);
			if (file != null && file.exists()) {
				try {
					final InputStream contents = file.getContents();
					final BufferedReader br = new BufferedReader(new InputStreamReader(contents));
					String line = null;
					String wid_const = QWickieActivator.WICKET_ID;
					while ((line = br.readLine()) != null) {
						if (line.contains(QWickieActivator.WICKET_DTD)) {
							wid_const = DocumentHelper.getWicketNamespace(line);
						}
						if (line.contains(wid_const + ":id=\"" + wicketId + "\"")) {
							text = "<b>Line in " + file.getName() + "</b><br>";
							text += Strings.trimLeadingTabsAndSpaces(line).replaceAll("<", "&lt;").replaceAll("\"" + wicketId + "\"",
									"<b>\"" + wicketId + "\"</b>");
							break;
						}
					}
					br.close();
					contents.close();
				} catch (final Exception e) {
				}
			}
		}
		return text;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.text.ITextHover#getHoverRegion(org.eclipse.jface.text
	 * .ITextViewer, int)
	 */
	public IRegion getHoverRegion(final ITextViewer textViewer, final int offset) {
		return null;
	}
}
