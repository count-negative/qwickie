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
package qwickie.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

import qwickie.QWickieActivator;

/**
 * @author count.negative
 * 
 */
public class QWickieProjectPreferencePage extends PropertyPage {

	public static final String ADDERS_TO = "addersTo";
	public static final String SEVERITY = "severity";
	public static final String EXCLUDES = "excludes";

	// private BooleanFieldEditor bEnable;
	private ComboFieldEditor cbSeverity;
	private RadioGroupFieldEditor rgAdder;
	private StringFieldEditor sfExcludes;

	public QWickieProjectPreferencePage() {
		// Set the preference store for the preference page.
		IPreferenceStore store = QWickieActivator.getDefault().getPreferenceStore();
		setPreferenceStore(store);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.
	 * swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(final Composite parent) {
		Composite composite = new Composite(parent, SWT.LEFT);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		String[][] severityValues = new String[3][2];
		severityValues[0][0] = "info";
		severityValues[0][1] = "0";
		severityValues[1][0] = "warning";
		severityValues[1][1] = "1";
		severityValues[2][0] = "error";
		severityValues[2][1] = "2";
		cbSeverity = new ComboFieldEditor(SEVERITY, "Show wicket errors as", severityValues, composite);
		cbSeverity.setPreferenceStore(getPreferenceStore());
		cbSeverity.setPage(this);
		cbSeverity.load();

		sfExcludes = new StringFieldEditor(EXCLUDES, "exclude project paths from being checked (comma separated)", composite);
		sfExcludes.setPreferenceStore(getPreferenceStore());
		sfExcludes.setPage(this);
		sfExcludes.load();

		rgAdder = new RadioGroupFieldEditor(ADDERS_TO, "add new components to", 1, new String[][] { { "Constructor", "ctor" }, { "onInitialize", "init" } },
				composite);
		rgAdder.setPreferenceStore(getPreferenceStore());
		rgAdder.setPage(this);
		rgAdder.load();

		return composite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		// bEnable.loadDefault();
		rgAdder.loadDefault();
		cbSeverity.loadDefault();
		sfExcludes.loadDefault();
		super.performDefaults();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		// bEnable.store();
		rgAdder.store();
		cbSeverity.store();
		sfExcludes.store();

		try {
			IProject project = (IProject) getElement().getAdapter(IProject.class);
			project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		} catch (CoreException e) {
		}

		return super.performOk();
	}

}
