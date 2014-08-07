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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.SourceType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.ltk.core.refactoring.resource.ResourceChange;

import qwickie.QWickieActivator;
import qwickie.preferences.QWickiePreferencePage;
import qwickie.util.FileSearcher;
import qwickie.util.TypeHelper;

/**
 * If a Java file of type Component is renamed, then rename the corresponding html file too
 * 
 * @author count.negative
 * 
 */
@SuppressWarnings("restriction")
public class QWickieTypeRenameParticipant extends RenameParticipant {

	private SourceType sourceType;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant# checkConditions(org.eclipse.core.runtime.IProgressMonitor,
	 * org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext)
	 */
	@Override
	public RefactoringStatus checkConditions(final IProgressMonitor paramIProgressMonitor, final CheckConditionsContext paramCheckConditionsContext) throws OperationCanceledException {
		return new RefactoringStatus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant# createChange(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public Change createChange(final IProgressMonitor paramIProgressMonitor) throws CoreException, OperationCanceledException {
		if (sourceType == null) {
			return null;
		}
		return new ResourceChange() {

			@Override
			public Change perform(final IProgressMonitor paramIProgressMonitor) throws CoreException {
				final IPath path = sourceType.getPath().removeLastSegments(1);
				IPath newPath = path;
				if (sourceType.getTypeQualifiedName().contains("$")) {
					newPath = path.append(sourceType.getDeclaringType().getElementName() + "$" + getArguments().getNewName());
				} else {
					newPath = path.append(getArguments().getNewName());
				}
				if (newPath.getFileExtension() == null) {
					newPath = newPath.addFileExtension("html");
				}
				final IFile oldFile = ResourcesPlugin.getWorkspace().getRoot().getFile(path.append(sourceType.getTypeQualifiedName() + ".html"));

				if (oldFile.exists() && oldFile.isAccessible()) { // if there's a file in the same folder
					oldFile.refreshLocal(SAVE_IF_DIRTY, paramIProgressMonitor);
					oldFile.move(newPath, true, true, paramIProgressMonitor);
				}

				FileSearcher fs = new FileSearcher(sourceType.getJavaProject().getProject(), sourceType.getTypeQualifiedName() + "*");
				ResourcesPlugin.getWorkspace().getRoot().accept(fs);
				List<IFile> ffs = fs.getFoundFiles();

				for (IFile ff : ffs) {
					if (ff != null && ff.exists() && ff.isAccessible() && FileSearcher.haveSameRelativePathToParentSourceFolder(ff, oldFile)) {
						IPath nfn = ff.getFullPath().removeLastSegments(1).append(getArguments().getNewName());
						if (ff.getName().startsWith(sourceType.getTypeQualifiedName() + "_")) { // variation
							nfn = ff.getFullPath().removeLastSegments(1).append(getArguments().getNewName() + ff.getName().substring(ff.getName().indexOf("_")));
						} else if (ff.getName().startsWith(sourceType.getTypeQualifiedName() + "$")) { // inner type
							nfn = ff.getFullPath().removeLastSegments(1).append(getArguments().getNewName() + ff.getName().substring(ff.getName().indexOf("$")));
						} else {
							nfn = nfn.addFileExtension("html");
						}
						ff.refreshLocal(SAVE_IF_DIRTY, paramIProgressMonitor);
						ff.move(nfn, true, true, paramIProgressMonitor);
					}
				}

				fs = new FileSearcher(sourceType.getJavaProject().getProject(), sourceType.getTypeQualifiedName() + ".properties");
				ResourcesPlugin.getWorkspace().getRoot().accept(fs);
				ffs = fs.getFoundFiles();

				for (IFile ff : ffs) {
					if (ff != null && ff.exists() && ff.isAccessible() && FileSearcher.haveSameRelativePathToParentSourceFolder(ff, oldFile)) {
						IPath nfn = ff.getFullPath().removeLastSegments(1).append(getArguments().getNewName());
						if (ff.getName().startsWith(sourceType.getTypeQualifiedName() + "_")) { // variation
							nfn = ff.getFullPath().removeLastSegments(1).append(getArguments().getNewName() + ff.getName().substring(ff.getName().indexOf("_")));
						} else if (ff.getName().startsWith(sourceType.getTypeQualifiedName() + "$")) { // inner type

						} else {
							nfn = nfn.addFileExtension("properties");
						}
						ff.refreshLocal(SAVE_IF_DIRTY, paramIProgressMonitor);
						ff.move(nfn, true, true, paramIProgressMonitor);
					}
				}
				return new NullChange();
			}

			@Override
			public String getName() {
				return "Rename wicket files '" + sourceType.getTypeQualifiedName() + "' to '" + getArguments().getNewName() + "'";
			}

			@Override
			protected IResource getModifiedResource() {
				return sourceType.getResource();
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant# getName()
	 */
	@Override
	public String getName() {
		return "Wicket type file rename refactoring";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant# initialize(java.lang.Object)
	 */
	@Override
	protected boolean initialize(final Object paramObject) {
		if (QWickieActivator.getDefault().getPreferenceStore().getBoolean(QWickiePreferencePage.REF_TYPES) && paramObject instanceof SourceType) {
			this.sourceType = (SourceType) paramObject;
			try {
				if (TypeHelper.isWicketJavaElement(sourceType)) {
					return true;
				}
			} catch (final JavaModelException e) {
			}
		}
		return false;
	}
}
