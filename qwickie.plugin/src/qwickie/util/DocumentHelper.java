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
package qwickie.util;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICodeAssist;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.internal.core.NamedMember;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import qwickie.QWickieActivator;

/**
 * @author count.negative
 *
 */
@SuppressWarnings("restriction")
public final class DocumentHelper {
	public static final String WICKET = "wicket";
	public static final String WICKET_MESSAGE = "wicket:message";
	public static final String KEY = "key";
	public static final String GET_STRING = "getString";
	public static final String RESOURCE_MODEL = "ResourceModel";
	public static final String STRING_RESOURCE_MODEL = "StringResourceModel";

	private DocumentHelper() {
	}

	/**
	 * finds an argument between "" <br>
	 * eg. new String("lalal");
	 */
	public static IRegion findStringArgumentInJava(final IDocument document, final int offset) {
		try {
			final IRegion li = document.getLineInformationOfOffset(offset);
			for (int i = offset - 1; i >= li.getOffset(); i--) {
				if (document.getChar(i) == '"') {
					for (int j = offset; j <= li.getOffset() + li.getLength(); j++) {
						if (document.getChar(j) == '"') {
							return new Region(i + 1, j - i - 1);
						}
					}
				}
			}
		} catch (final BadLocationException e) {
		}
		return null;
	}

	public static IRegion findWord(final IDocument document, final int offset) {
		int start = -2;
		int end = -1;
		try {
			int pos = offset;

			while (pos >= 0) {
				char c = document.getChar(pos);
				if ((!(Character.isJavaIdentifierPart(c))) && (!(((55296 <= c) && (c <= 57343))))) {
					break;
				}

				--pos;
			}
			start = pos;

			pos = offset;
			int length = document.getLength();

			while (pos < length) {
				char c = document.getChar(pos);
				if ((!(Character.isJavaIdentifierPart(c))) && (!(((55296 <= c) && (c <= 57343))))) {
					break;
				}

				++pos;
			}
			end = pos;
		} catch (BadLocationException localBadLocationException) {
		}
		if ((start >= -1) && (end > -1)) {
			if ((start == offset) && (end == offset)) {
				return new Region(offset, 0);
			}
			if (start == offset) {
				return new Region(start, end - start);
			}
			return new Region(start + 1, end - start - 1);
		}

		return null;
	}

	public static String getNamespacePrefix(final IDocument document) {
		Assert.isNotNull(document);
		FindReplaceDocumentAdapter frda = new FindReplaceDocumentAdapter(document);
		try {
			final IRegion dtd = frda.find(0, QWickieActivator.WICKET_DTD, true, false, false, false);
			if (dtd != null) {
				final IRegion li = document.getLineInformationOfOffset(dtd.getOffset());
				final String line = document.get(li.getOffset(), li.getLength());
				return getWicketNamespace(line);
			}
		} catch (BadLocationException e) {
		}
		return WICKET;
	}

	public static String getWicketNamespace(final String line) {
		int start = line.indexOf("xmlns:");
		if (start != -1) {
			return line.substring(start + 6, line.indexOf("=", start));
		}
		return WICKET;
	}

	public static IRegion findStringArgumentInMarkup(final IDocument document, final int offset, final String argument) {
		try {
			final IRegion lineInfo = document.getLineInformationOfOffset(offset);
			final String line = document.get(lineInfo.getOffset(), lineInfo.getLength());
			final int lineRelativeOffset = offset - lineInfo.getOffset();
			final int firstG = line.lastIndexOf('"', lineRelativeOffset - 1);
			final int lastG = line.indexOf('"', lineRelativeOffset);
			if (firstG == -1 || firstG == lastG) {
				return null;
			}
			final int start = line.lastIndexOf(" ", firstG) + 1;
			final int end = firstG - 1;
			if (start >= end) {
				return null;
			}
			final CharSequence attribute = line.subSequence(start, end);
			if (!attribute.equals(argument) && !KEY.equals(attribute)) {
				return null;
			}
			// could be a <wicket:message key="linkText"/>
			if (KEY.equals(attribute) && line.lastIndexOf(argument, firstG - KEY.length()) == -1) {
				return null;
			}
			return new Region(offset - (lineRelativeOffset - firstG - 1), lastG - firstG - 1);
		} catch (final BadLocationException e) {
			return null;
		}
	}

	/** Finds every wicket:id element in a java editor */
	public static IRegion[] findWicketRegions(final IEditorPart editor, final IDocument document, final int offset) {
		Assert.isNotNull(editor);
		Assert.isNotNull(document);

		final IRegion[] regions = new IRegion[3];

		try {
			IRegion wicketIdRegion = findStringArgumentInJava(document, offset);
			if (wicketIdRegion == null) {
				return null;
			}
			final IRegion wicketComponentRegion = DocumentHelper.findWicketComponentRegion(document, wicketIdRegion.getOffset());
			if (wicketComponentRegion == null) {
				return null;
			}
			final IRegion javaRegion = findWord(document, wicketComponentRegion.getOffset());
			if (javaRegion.getLength() == 0) {
				return null;
			}
			final IJavaElement input = EditorUtility.getEditorInputJavaElement(editor, false);
			final IJavaElement[] javaElements = ((ICodeAssist) input).codeSelect(javaRegion.getOffset(), javaRegion.getLength());
			if (javaElements == null || javaElements.length == 0) {
				return null;
			}
			for (final IJavaElement javaElement : javaElements) {
				final boolean isWicketComponent = TypeHelper.isWicketComponent(javaElement);
				// search for a string in a properties file
				if (STRING_RESOURCE_MODEL.equals(javaElement.getElementName()) || RESOURCE_MODEL.equals(javaElement.getElementName())
						|| (GET_STRING.equals(javaElement.getElementName()) && isWicketComponent)) {
					regions[2] = findStringArgumentInJava(document, offset);
					regions[1] = regions[2];
				} else {
					regions[1] = DocumentHelper.getRegionOfWicketComponent(document, offset, javaElement);
				}
			}
			regions[0] = wicketIdRegion;
			return regions;
		} catch (final JavaModelException e) {
			return null;
		}
	}

	/**
	 * Finds the IRegion where a wicket Component (one that extends Component)
	 * is found
	 */
	public static IRegion getRegionOfWicketComponent(final IDocument document, final int offset, final IJavaElement javaElement) throws JavaModelException {
		if (javaElement != null && javaElement instanceof NamedMember) {
			final NamedMember method = (NamedMember) javaElement;
			final IType type = method.getDeclaringType();
			if (type != null) {
				final ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
				if (hierarchy != null) {
					final IType[] supertypes = hierarchy.getAllSupertypes(type);
					for (final IType iType : supertypes) {
						if (iType.getFullyQualifiedName().equals(TypeHelper.COMPONENT)) {
							try {
								final IRegion lineInfo = document.getLineInformationOfOffset(offset);
								final String line = document.get(lineInfo.getOffset(), lineInfo.getLength());
								final int lineRelativeOffset = offset - lineInfo.getOffset();
								final int index = line.indexOf(javaElement.getElementName());
								return new Region(offset - lineRelativeOffset + index, javaElement.getElementName().length());
							} catch (final Exception ex) {
								return null;
							}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * finds the Region where a wicket component is created
	 *
	 * @param document
	 *            the java document
	 * @param offset
	 *            where the search should begin
	 * @return A region where the java component part begins (e.g. the 'F' in
	 *         "new Form<Customer>("customerForm");" length is always 1
	 */
	public static IRegion findWicketComponentRegion(final IDocument document, final int offset) {
		try {
			final IRegion lineInfo = document.getLineInformationOfOffset(offset);
			final String line = document.get(lineInfo.getOffset(), lineInfo.getLength());
			final int lineRelativeOffset = offset - lineInfo.getOffset();
			int start = lineRelativeOffset;
			boolean qFound = false;
			for (int i = line.length() == lineRelativeOffset ? lineRelativeOffset - 1 : lineRelativeOffset; i > 0; i--) {
				final char c = line.charAt(i);
				if (c == '(' || c == ',') {
					start = i;
					break;
				}
				if (!qFound && c == '"') {
					qFound = true;
				} else if (qFound && c == '"') {
					return null;
				}
			}
			int funcStart = line.lastIndexOf(GET_STRING, start) + 1;
			if (funcStart == 0) {
				for (int i = start - 1; i > 0; i--) {
					final char c = line.charAt(i);
					if (c == '(' || c == ' ') {
						funcStart = i + 1;
						break;
					}
				}
			}
			return new Region(offset - lineRelativeOffset + funcStart, 1);
		} catch (final BadLocationException e) {
			return null;
		}
	}

	/**
	 * marks all occurrences of the given string
	 */
	public static void markOccurrence(final ITextEditor textEditor, final String string) {
		if (string == null) {
			return;
		}
		SearchPattern pattern = SearchPattern.createPattern(string, IJavaSearchConstants.FIELD, IJavaSearchConstants.ALL_OCCURRENCES,
				SearchPattern.R_EXACT_MATCH);

		SearchRequestor requestor = new SearchRequestor() {
			@Override
			public void acceptSearchMatch(final SearchMatch match) {
				IAnnotationModel model = textEditor.getDocumentProvider().getAnnotationModel(textEditor.getEditorInput());
				Annotation annotation = new Annotation("org.eclipse.jdt.ui.occurrences", false, "wicket id constant");
				model.addAnnotation(annotation, new Position(match.getOffset(), match.getLength()));
			}
		};

		SearchEngine searchEngine = new SearchEngine();
		try {
			searchEngine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, SearchEngine.createWorkspaceScope(), requestor,
					new NullProgressMonitor());
		} catch (CoreException e) {
		}
	}

	/**
	 * returns the String constant name in a line of java code e.g.
	 * <code>protected String PETE = "pete";</code> returns <code>PETE</code>
	 */
	public static String getStringConstantName(final String line) {
		Assert.isNotNull(line);
		final int ios = line.indexOf("String ");
		if (ios == -1) {
			return null;
		}
		return line.substring(ios + 6, line.indexOf('=')).trim();
	}
}
