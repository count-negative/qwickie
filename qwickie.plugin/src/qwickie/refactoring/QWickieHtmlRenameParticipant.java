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

import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameTypeProcessor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

import qwickie.QWickieActivator;
import qwickie.hyperlink.WicketHyperlink;
import qwickie.preferences.QWickiePreferencePage;
import qwickie.util.FileSearcher;
import qwickie.util.TypeHelper;

/**
 * @author count.negative
 * 
 */
@SuppressWarnings("restriction")
public class QWickieHtmlRenameParticipant extends RenameParticipant {

	private File htmlFile;
	private IFile javaFile;
	private ICompilationUnit compilationUnit;

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
		if (compilationUnit == null || javaFile == null) {
			return null;
		}
		final String newName = getArguments().getNewName().replaceAll("\\.html", "");

		if (newName.contains("_")) {
			// don't rename java file in case of a variation
			return new NullChange("Java file part of wicket won't be renamed.");
		}
		final RenameTypeProcessor jrp = new RenameTypeProcessor(compilationUnit.getAllTypes()[0]);
		final RenameRefactoring rr = new RenameRefactoring(jrp);
		jrp.setNewElementName(newName);
		jrp.setUpdateQualifiedNames(true);
		jrp.setUpdateReferences(true);
		rr.checkAllConditions(paramIProgressMonitor);
		return rr.createChange(paramIProgressMonitor);
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
		return "Wicket html file rename refactoring";
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
		if (QWickieActivator.getDefault().getPreferenceStore().getBoolean(QWickiePreferencePage.REF_HTML) && paramObject instanceof File) {
			this.htmlFile = (File) paramObject;
			if (htmlFile.getFileExtension() != null && htmlFile.getFileExtension().equalsIgnoreCase("html")) {
				javaFile = WicketHyperlink.getJavaFile(htmlFile);

				if (javaFile == null || !javaFile.exists() || !FileSearcher.haveSameRelativePathToParentSourceFolder(javaFile, htmlFile)) {
					return false; // there is no Java File with this name
				}

				final ICompilationUnit compUnit = JavaCore.createCompilationUnitFrom(javaFile);
				try {
					final IType[] types = compUnit.getTypes();
					if (types.length > 0) {
						final SourceType type = (SourceType) types[0];
						final boolean isWicketComponent = TypeHelper.isWicketJavaElement(type);
						if (isWicketComponent) {
							this.compilationUnit = compUnit;
							return isWicketComponent;
						}
					}
				} catch (final JavaModelException e) {
				}
				return true;
			}
		}
		return false;
	}

}
