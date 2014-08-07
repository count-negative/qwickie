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
package qwickie.quickfix;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;

import qwickie.builder.QWickieBuilder;

public class QWickieQuickFixProcessor implements IQuickFixProcessor {

	public boolean hasCorrections(final ICompilationUnit unit, final int problemId) {
		return false;
	}

	public IJavaCompletionProposal[] getCorrections(final IInvocationContext invocationContext, final IProblemLocation[] locations) throws CoreException {
		List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();
		final IMarker[] markers = invocationContext.getCompilationUnit().getResource().findMarkers(QWickieBuilder.MARKER_TYPE, false, 0);
		for (IMarker marker : markers) {
			final QWickieQuickFixProposal qqfp = new QWickieQuickFixProposal(invocationContext.getSelectionOffset(), invocationContext.getSelectionLength(), marker);
			if (qqfp.getDisplayString() != null) {
				proposals.add(qqfp);
			}
		}

		return proposals.toArray(new IJavaCompletionProposal[proposals.size()]);
	}

}
