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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;

import qwickie.util.DocumentHelper;

/**
 * Hyperlinker should open the html file and jump to the corresponding wicket:id
 * 
 * @author count.negative
 * 
 */
public class QWickieHtmlHyperlinkDetector extends AbstractHyperlinkDetector {

	private String extensionToOpen = "java";

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.jdt.internal.ui.javaeditor.JavaElementHyperlinkDetector#
	 * detectHyperlinks(org.eclipse.jface.text.ITextViewer,
	 * org.eclipse.jface.text.IRegion, boolean)
	 */
	public IHyperlink[] detectHyperlinks(final ITextViewer textViewer, final IRegion region, final boolean canShowMultipleHyperlinks) {
		if ((region == null) || (textViewer == null)) {
			return null;
		}
		final int offset = region.getOffset();

		final IDocument document = textViewer.getDocument();
		if (document == null) {
			return null;
		}

		String wicketId = null;
		final IRegion widRegion = findWicketId(textViewer, offset);
		if (widRegion == null) {
			return null;
		}

		try {
			wicketId = document.get(widRegion.getOffset(), widRegion.getLength());
			if (wicketId == null) {
				return null;
			}

		} catch (final Exception e) {
			return null;
		}

		final WicketHyperlink wicketHyperlink = new WicketHyperlink(widRegion, wicketId, extensionToOpen);
		wicketHyperlink.openJavaFileOnly(extensionToOpen.equals(WicketHyperlink.JAVA));

		return new IHyperlink[] { wicketHyperlink };
	}

	/**
	 * Find the region where a wicket:id is in
	 */
	private IRegion findWicketId(final ITextViewer textViewer, final int offset) {
		Assert.isNotNull(textViewer);
		final IDocument document = textViewer.getDocument();
		return findWicketId(document, offset);
	}

	/**
	 * Find the region where a wicket:id is in
	 */
	public IRegion findWicketId(final IDocument document, final int offset) {
		Assert.isNotNull(document);

		final String wid_const = DocumentHelper.getNamespacePrefix(document);
		IRegion wordRegion = DocumentHelper.findStringArgumentInMarkup(document, offset, wid_const + ":id");
		if (wordRegion == null) {
			wordRegion = DocumentHelper.findStringArgumentInMarkup(document, offset, "wicket:message");
			if (wordRegion != null) {
				extensionToOpen = "properties";
			}
		} else {
			extensionToOpen = "java";
		}
		return wordRegion;
	}
}