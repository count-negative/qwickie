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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class FileSearcher implements IResourceVisitor {

	private final IProject project;
	private final List<IPath> sourceRoots;
	private final String filename;
	private final String extension;
	private final List<IFile> foundFiles = new ArrayList<IFile>();
	private final boolean wildcardSearch;

	public FileSearcher(final IProject project, final String filename) {
		Assert.isNotNull(project);
		Assert.isNotNull(filename);

		this.project = project;
		sourceRoots = getSourceFolders(project);
		wildcardSearch = filename.contains("*");
		if (!wildcardSearch) {
			this.filename = filename.substring(0, filename.lastIndexOf('.'));
			this.extension = filename.substring(filename.lastIndexOf('.'));
		} else {
			if (filename.contains(".")) {
				this.extension = filename.substring(filename.lastIndexOf('.'));
				this.filename = filename.substring(0, filename.lastIndexOf('.')).replaceAll("\\*", "");
			} else {
				extension = "";
				this.filename = filename.replaceAll("\\*", "");
			}
		}
	}

	public List<IFile> getFoundFiles() {
		return foundFiles;
	}

	public IFile getFoundFile() {
		if (getFoundFiles().size() > 0) {
			return getFoundFiles().get(0);
		} else {
			return null;
		}
	}

	public boolean visit(final IResource resource) throws CoreException {

		if (resource.getType() == IResource.FILE) {
			final String folder = resource.getFullPath().toPortableString();
			for (final IPath sourceRoot : sourceRoots) {
				if (folder.startsWith(sourceRoot.toPortableString())) {
					final String cf = resource.getFullPath().lastSegment();
					if (wildcardSearch ? cf.startsWith(filename) : cf.endsWith(extension)) {
						if (sourceRoot.segment(1).equals(resource.getFullPath().segment(1))) {
							if (cf.equals(filename + extension) || cf.startsWith(filename + "_") || cf.startsWith(filename + "$")) { // look for wicket variants, and inner types
								foundFiles.add((IFile) resource);
								return false; // if one found, don't go further
							}
						}
					}
				}
			}
		}
		return true;
	}

	/** return the list of IPath configured as source folders in the project */
	public static List<IPath> getSourceFolders(final IProject project) {
		Assert.isNotNull(project);
		final List<IPath> srcFolders = new ArrayList<IPath>();
		IJavaProject javaProject;
		try {
			javaProject = (IJavaProject) project.getNature(JavaCore.NATURE_ID);
			final IPackageFragmentRoot[] packageFragmentRoots = javaProject.getPackageFragmentRoots();
			for (final IPackageFragmentRoot pfr : packageFragmentRoots) {
				if (pfr.getKind() == IPackageFragmentRoot.K_SOURCE) {
					srcFolders.add(pfr.getPath());
				}
			}
		} catch (final CoreException e) {
		}
		return srcFolders;
	}

	/**
	 * Takes a full filename and removes any possible source folders
	 * @param project used to get the source folders
	 * @param fileName the fullpath as portable string
	 * @return the fullpath without any source folders
	 */
	public static String removeSourceFolder(final IProject project, String fileName) {
		Assert.isNotNull(fileName);
		final List<IPath> srcFolders = getSourceFolders(project);
		for (IPath iPath : srcFolders) {
			fileName = fileName.replace(iPath.toPortableString(), "");
		}
		return fileName;
	}

	/**
	 * Checks whether the two resources are in the same relative path to their respective source folders. The resources have to be in the same (java) project.
	 * returns true if the resources have the same relative path, false in all other cases.
	 * 
	 * @param one
	 *            , the one resource you would like to check
	 * @param other
	 *            , the one resource you would like to check
	 * @return true for resources in the same relative path, false otherwise.
	 */
	public static boolean haveSameRelativePathToParentSourceFolder(final IResource one, final IResource other) {
		// both resources have to be non-null
		if (one == null || other == null) {
			return false;
		}

		IProject project = one.getProject();
		// if the resources are in different projects, return false
		if (!project.equals(other.getProject())) {
			return false;
		}

		IJavaProject javaProject = null;
		try {
			javaProject = (IJavaProject) project.getNature(JavaCore.NATURE_ID);
		} catch (CoreException e) {
			// When it's not a java project, return false
			return false;
		}

		IPath onePath = one.getParent().getProjectRelativePath();
		IPath otherPath = other.getParent().getProjectRelativePath();

		IPath srcPath;
		boolean oneFound = false, otherFound = false;

		try {
			for (IPackageFragmentRoot pfr : javaProject.getPackageFragmentRoots()) {
				if (pfr.getKind() == 1) {
					// we've got a source path
					// remove the first segment, since that's the project folder.
					srcPath = pfr.getPath().removeFirstSegments(1);
					if (!oneFound && srcPath.isPrefixOf(onePath)) {
						// remove the sourcepath from this path
						onePath = onePath.removeFirstSegments(srcPath.segmentCount());
						oneFound = true;
					}
					if (!otherFound && srcPath.isPrefixOf(otherPath)) {
						// remove the sourcepath from this path
						otherPath = otherPath.removeFirstSegments(srcPath.segmentCount());
						otherFound = true;
					}
				}
				if (oneFound && otherFound) {
					break;
				}
			}
		} catch (JavaModelException e) {
			return false;
		}

		// return true if both paths are the same
		return onePath.equals(otherPath);
	}

	@Override
	public String toString() {
		return "FileSearcher [project=" + project + ", filename=" + filename + ", foundFiles=" + foundFiles + ", wildcardSearch=" + wildcardSearch + "]";
	}
}
