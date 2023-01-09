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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import qwickie.QWickieActivator;

/**
 * @author count.negative
 */
public class WicketComponentHyperlink implements IHyperlink {

	private final String wcName;
	private final IRegion region;
	private IFile file;
	private final IPreferenceStore store = QWickieActivator.getDefault().getPreferenceStore();
	private List<String> excludes = new ArrayList<String>();

	public WicketComponentHyperlink(final IRegion region, final String wcName) {
		Assert.isNotNull(wcName);
		Assert.isNotNull(region);

		this.region = region;
		this.wcName = wcName;
		excludes = Arrays.asList(store.getString("excludes").split(","));
		findFile(wcName + ".html");
	}

	@Override
	public IRegion getHyperlinkRegion() {
		return this.region;
	}

	@Override
	public void open() {
		if (this.wcName != null) {
			final IWorkbench workbench = PlatformUI.getWorkbench();
			final IWorkbenchPage activePage = workbench.getActiveWorkbenchWindow().getActivePage();

			try {
				if (file != null && file.exists()) {
					IDE.openEditor(activePage, file, true);
				}
			} catch (final PartInitException pie) {
				return;
			}
		}
	}

	private void findFile(final String filename) {
		if (filename != null) {
			IResource[] members;
			try {
				members = ResourcesPlugin.getWorkspace().getRoot().members();
				for (int i = 0; i < members.length; i++) {
					final IResource member = members[i];

					if (member.isAccessible()) {
						member.getProject().accept(new IResourceVisitor() {

							@Override
							public boolean visit(final IResource resource) throws CoreException {
								if (resource.getName().equals(filename)) {
									boolean excluded = false;
									for (final String exclude : excludes) {
										final String[] segs = resource.getFullPath().segments();
										for (final String seg : segs) {
											if (seg.equals(exclude)) {
												excluded = true;
												break;
											}
										}
									}
									if (!excluded) {
										file = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(resource.getLocationURI())[0];
										return false;
									}
								}
								return true;
							}
						});
					}
				}
			} catch (final CoreException e1) {
			}
		}
	}

	@Override
	public String getTypeLabel() {
		return null;
	}

	@Override
	public String getHyperlinkText() {
		if (file == null) {
			return null;
		}
		return "Open \"" + wcName + ".html\"";
	}

	public String getURLString() {
		return this.wcName;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " for wicket Component " + wcName + " to " + file;
	}
}