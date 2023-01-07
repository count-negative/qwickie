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
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
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
@SuppressWarnings("restriction")
public class QWickieJavaHyperlinkDetector extends AbstractHyperlinkDetector {

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
		final JavaEditor editor = getAdapter(JavaEditor.class);
		Assert.isNotNull(editor);

		String wicketId = null;
		String wcName = null;
		/* java file region, html file region, properties file region */
		final IRegion[] wicketRegions = DocumentHelper.findWicketRegions(editor, document, offset);
		if (wicketRegions == null) {
			return null;
		}
		final IRegion widRegion = wicketRegions[0];
		final IRegion wcRegion = wicketRegions[1];
		final IRegion wcPropertiesRegion = wicketRegions[2];
		if (widRegion == null || wcRegion == null) {
			return null;
		}

		try {
			wicketId = document.get(widRegion.getOffset(), widRegion.getLength());
			wcName = document.get(wcRegion.getOffset(), wcRegion.getLength());
			if (wicketId == null || wcName == null) {
				return null;
			}

		} catch (final Exception e) {
			return null;
		}

		final IHyperlink wicketHyperlink = new WicketHyperlink(widRegion, wicketId, wcPropertiesRegion == null ? "html" : "properties");
		final IHyperlink wcHyperlink = new WicketComponentHyperlink(wcRegion, wcName);

		if (wcHyperlink.getHyperlinkText() != null) {
			return new IHyperlink[] { wicketHyperlink, wcHyperlink };
		} else {
			return new IHyperlink[] { wicketHyperlink };
		}
	}
}