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
package qwickie.builder;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class QWickieToggleNatureAction implements IObjectActionDelegate {

	private static final String ADD = "Add QWickie Nature";
	private static final String REMOVE = "Remove QWickie Nature";
	private ISelection selection;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(final IAction action) {
		final IProject project = getProject(selection);
		if (project != null) {
			if (toggleNature(project)) {
				action.setText(REMOVE);
			} else {
				action.setText(ADD);
			}
			try {
				project.deleteMarkers(QWickieBuilder.MARKER_TYPE, true, IProject.DEPTH_INFINITE);
				project.build(IncrementalProjectBuilder.FULL_BUILD, null);
			} catch (final CoreException e) {
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.
	 * IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(final IAction action, final ISelection selection) {
		this.selection = selection;

		final IProject project = getProject(selection);
		if (project != null) {
			try {
				if (project.hasNature(QWickieNature.NATURE_ID)) {
					action.setText(REMOVE);
				} else {
					action.setText(ADD);
				}
			} catch (final CoreException e) {
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.
	 * action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
	}

	/**
	 * Toggles sample nature on a project
	 * 
	 * @param project
	 *            to have sample nature added or removed
	 */
	private boolean toggleNature(final IProject project) {
		try {
			final IProjectDescription description = project.getDescription();
			final String[] natures = description.getNatureIds();

			for (int i = 0; i < natures.length; ++i) {
				if (QWickieNature.NATURE_ID.equals(natures[i])) {
					// Remove the nature
					final String[] newNatures = new String[natures.length - 1];
					System.arraycopy(natures, 0, newNatures, 0, i);
					System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
					description.setNatureIds(newNatures);
					project.setDescription(description, null);
					return false;
				}
			}

			// Add the nature
			final String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = QWickieNature.NATURE_ID;
			description.setNatureIds(newNatures);
			project.setDescription(description, null);

		} catch (final CoreException e) {
		}
		return true;
	}

	private static IProject getProject(final ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			for (final Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();) {
				final Object element = it.next();
				IProject project = null;
				if (element instanceof IProject) {
					project = (IProject) element;
				} else if (element instanceof IAdaptable) {
					project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
				}
				return project;
			}
		}
		return null;
	}

}
