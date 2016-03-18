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
package qwickie.refactoring;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchMatchAccess;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEditGroup;

import qwickie.QWickieActivator;
import qwickie.preferences.QWickiePreferencePage;

/**
 * @author count.negative
 * 
 */
@SuppressWarnings("restriction")
public class QWickieFieldRenameParticipant extends RenameParticipant {

	private SourceField sourceField;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#
	 * checkConditions(org.eclipse.core.runtime.IProgressMonitor,
	 * org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext)
	 */
	@Override
	public RefactoringStatus checkConditions(final IProgressMonitor paramIProgressMonitor, final CheckConditionsContext paramCheckConditionsContext)
			throws OperationCanceledException {
		return new RefactoringStatus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#
	 * createChange(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public Change createChange(final IProgressMonitor paramIProgressMonitor) throws CoreException, OperationCanceledException {
		if (sourceField == null) {
			return null;
		}
		final Map<IFile, TextFileChange> changes = new HashMap<IFile, TextFileChange>();
		final String newName = getArguments().getNewName();

		final IResource[] roots = { sourceField.getJavaProject().getProject() };
		processHtmlFiles(paramIProgressMonitor, changes, newName, roots);
		processJavaFiles(paramIProgressMonitor, changes, newName, roots);

		if (changes.isEmpty()) {
			return null;
		}

		final CompositeChange result = new CompositeChange("QWickie renamings");
		for (final Iterator<TextFileChange> iter = changes.values().iterator(); iter.hasNext();) {
			result.add(iter.next());
		}
		return result;
	}

	protected void processHtmlFiles(final IProgressMonitor paramIProgressMonitor, final Map<IFile, TextFileChange> changes, final String newName,
			final IResource[] roots) {
		final String[] fileNamePatterns = { "*.html" };
		final FileTextSearchScope scope = FileTextSearchScope.newSearchScope(roots, fileNamePatterns, false);
		final Pattern pattern = Pattern.compile(QWickieActivator.WICKET_ID + "=\"" + sourceField.getElementName() + "\"");
		final String replacement = QWickieActivator.WICKET_ID + "=\"" + newName + "\"";

		final TextSearchRequestor collector = new TextSearchRequestor() {
			@Override
			public boolean acceptPatternMatch(final TextSearchMatchAccess matchAccess) throws CoreException {
				final IFile file = matchAccess.getFile();
				TextFileChange change = changes.get(file);
				if (change == null) {
					final TextChange textChange = getTextChange(file);
					if (textChange != null) {
						return false;
					}
					change = new TextFileChange(file.getName(), file);
					change.setEdit(new MultiTextEdit());
					changes.put(file, change);
				}
				final ReplaceEdit edit = new ReplaceEdit(matchAccess.getMatchOffset(), matchAccess.getMatchLength(), replacement);
				change.addEdit(edit);
				change.addTextEditGroup(new TextEditGroup("Update " + QWickieActivator.WICKET_ID, edit));
				return true;
			}
		};

		TextSearchEngine.create().search(scope, collector, pattern, paramIProgressMonitor);
	}

	private void processJavaFiles(final IProgressMonitor paramIProgressMonitor, final Map<IFile, TextFileChange> changes, final String newName,
			final IResource[] roots) {
		final String[] fileNamePatterns = { "*.java" };
		final FileTextSearchScope scope = FileTextSearchScope.newSearchScope(roots, fileNamePatterns, false);
		// TODO: find out if it's a wicket component with a PropertyModel
		final Pattern pattern = Pattern.compile("\"" + sourceField.getElementName() + "\"");
		final String replacement = "\"" + newName + "\"";

		final TextSearchRequestor collector = new TextSearchRequestor() {
			@Override
			public boolean acceptPatternMatch(final TextSearchMatchAccess matchAccess) throws CoreException {
				final IFile file = matchAccess.getFile();
				TextFileChange change = changes.get(file);
				if (change == null) {
					final TextChange textChange = getTextChange(file);
					if (textChange != null) {
						return false;
					}
					change = new TextFileChange(file.getName(), file);
					change.setEdit(new MultiTextEdit());
					changes.put(file, change);
				}
				final ReplaceEdit edit = new ReplaceEdit(matchAccess.getMatchOffset(), matchAccess.getMatchLength(), replacement);
				change.addEdit(edit);
				change.addTextEditGroup(new TextEditGroup("Update " + QWickieActivator.WICKET_ID, edit));
				return true;
			}
		};

		TextSearchEngine.create().search(scope, collector, pattern, paramIProgressMonitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#
	 * getName()
	 */
	@Override
	public String getName() {
		return QWickieActivator.WICKET_ID + " rename refactoring";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant#
	 * initialize(java.lang.Object)
	 */
	@Override
	protected boolean initialize(final Object paramObject) {
		if (QWickieActivator.getDefault().getPreferenceStore().getBoolean(QWickiePreferencePage.REF_FIELDS) && paramObject instanceof SourceField) {
			this.sourceField = (SourceField) paramObject;
			return true;
		}
		return false;
	}
}
