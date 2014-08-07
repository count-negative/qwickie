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

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.GlobalBuildAction;

import qwickie.QWickieActivator;

/**
 * @author count.negative
 * 
 */
public class QWickiePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public static final String OPEN_HTML_FILES = "openHTMLFiles";
	public static final String OPEN_PROPERTIES_FILES = "openPropertiesFiles";
	public static final String ADD_NEW_COMPONENTS = "addNewComponents";
	public static final String SEVERITY = "severity";
	public static final String EXCLUDES = "excludes";
	public static final String REF_TYPES = "refTypes";
	public static final String REF_HTML = "refHTML";
	public static final String REF_FIELDS = "refFields";

	private Button bOpenHTMLFiles;
	private Button bOpenPropertiesFiles;
	private Combo cbSeverity;
	private Button bCTOR;
	private Button bINIT;
	private Button bRefactorType;
	private Button bRefactorHTML;
	private Button bRefactorFields;
	private Text txtExcludes;

	private boolean rebuildNeeded;

	public enum SEVERITIES {
		info, warning, error
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(final Composite parent) {
		Composite composite = new Composite(parent, SWT.LEFT | SWT.TOP);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		addOpenGroup(composite);
		addNewComponentsGroup(composite);
		addRefactorGroup(composite);

		Group group = new Group(composite, NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText("When QWickie nature is enabled... (changing one of these executes a full build)");
		addSeverity(group);
		addExcludes(group);

		return composite;
	}

	private void addOpenGroup(final Composite parent) {
		Group group = new Group(parent, NONE);
		group.setLayout(new RowLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText("When a wicket java file is opened/closed, also open/close");

		bOpenHTMLFiles = new Button(group, SWT.CHECK | SWT.LEFT);
		bOpenHTMLFiles.setText("html files");
		bOpenHTMLFiles.setSelection(getPreferenceStore().getBoolean(OPEN_HTML_FILES));
		SelectionListener listener = new SelectionListener() {

			public void widgetSelected(final SelectionEvent e) {
				if (bOpenHTMLFiles.getSelection() || bOpenPropertiesFiles.getSelection()) {
					QWickieActivator.getDefault().addPartListener();
				} else {
					QWickieActivator.getDefault().removePartListener();
				}
			}

			public void widgetDefaultSelected(final SelectionEvent e) {
			}
		};
		bOpenHTMLFiles.addSelectionListener(listener);

		bOpenPropertiesFiles = new Button(group, SWT.CHECK | SWT.LEFT);
		bOpenPropertiesFiles.setText("properties files");
		bOpenPropertiesFiles.setSelection(getPreferenceStore().getBoolean(OPEN_PROPERTIES_FILES));
		bOpenPropertiesFiles.addSelectionListener(listener);
	}

	private void addNewComponentsGroup(final Composite parent) {
		Group group = new Group(parent, NONE);
		group.setLayout(new GridLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText("Add new components to");

		bCTOR = new Button(group, SWT.RADIO);
		bCTOR.setText("Constructor");
		bCTOR.setSelection("ctor".equals(getPreferenceStore().getString(ADD_NEW_COMPONENTS)));

		bINIT = new Button(group, SWT.RADIO);
		bINIT.setText("onInitialize");
		bINIT.setSelection("init".equals(getPreferenceStore().getString(ADD_NEW_COMPONENTS)));
	}

	private void addRefactorGroup(final Composite parent) {
		Group group = new Group(parent, NONE);
		group.setLayout(new RowLayout());
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		group.setText("Refactor rename");

		bRefactorType = new Button(group, SWT.CHECK);
		bRefactorType.setText("types");
		bRefactorType.setSelection(getPreferenceStore().getBoolean(REF_TYPES));

		bRefactorHTML = new Button(group, SWT.CHECK);
		bRefactorHTML.setText("html files");
		bRefactorHTML.setSelection(getPreferenceStore().getBoolean(REF_HTML));

		bRefactorFields = new Button(group, SWT.CHECK);
		bRefactorFields.setText("bean fields via PropertyModels");
		bRefactorFields.setSelection(getPreferenceStore().getBoolean(REF_FIELDS));
	}

	private void addExcludes(final Composite parent) {
		new Label(parent, SWT.NONE).setText("exclude project paths from being checked (comma separated)");

		txtExcludes = new Text(parent, SWT.SINGLE | SWT.BORDER);
		txtExcludes.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtExcludes.setText(getPreferenceStore().getString(EXCLUDES));
		txtExcludes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				rebuildNeeded = !(txtExcludes.getText().equals(getPreferenceStore().getString(EXCLUDES)));
			}
		});
	}

	private void addSeverity(final Composite parent) {
		new Label(parent, SWT.NONE).setText("show wicket errors as");

		cbSeverity = new Combo(parent, SWT.READ_ONLY);
		cbSeverity.add(SEVERITIES.info.name());
		cbSeverity.add(SEVERITIES.warning.name());
		cbSeverity.add(SEVERITIES.error.name());
		String ssever = getPreferenceStore().getString(QWickiePreferencePage.SEVERITY);
		try {
			int sever = Integer.parseInt(ssever);
			ssever = QWickiePreferencePage.SEVERITIES.values()[sever].name();
		} catch (NumberFormatException nfe) {
		}
		cbSeverity.setText(ssever);
		cbSeverity.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				rebuildNeeded = !(cbSeverity.getText().equals(getPreferenceStore().getString(SEVERITY)));
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		bCTOR.setSelection("ctor".equals(getPreferenceStore().getDefaultString(ADD_NEW_COMPONENTS)));
		bINIT.setSelection("init".equals(getPreferenceStore().getDefaultString(ADD_NEW_COMPONENTS)));
		bOpenHTMLFiles.setSelection(getPreferenceStore().getDefaultBoolean(OPEN_HTML_FILES));
		bOpenPropertiesFiles.setSelection(getPreferenceStore().getDefaultBoolean(OPEN_PROPERTIES_FILES));
		bRefactorType.setSelection(getPreferenceStore().getDefaultBoolean(REF_TYPES));
		bRefactorHTML.setSelection(getPreferenceStore().getDefaultBoolean(REF_HTML));
		bRefactorFields.setSelection(getPreferenceStore().getDefaultBoolean(REF_FIELDS));
		txtExcludes.setText(getPreferenceStore().getDefaultString(EXCLUDES));
		cbSeverity.setText(getPreferenceStore().getDefaultString(SEVERITY));
		super.performDefaults();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		getPreferenceStore().setValue(OPEN_HTML_FILES, bOpenHTMLFiles.getSelection());
		getPreferenceStore().setValue(OPEN_PROPERTIES_FILES, bOpenPropertiesFiles.getSelection());
		getPreferenceStore().setValue(ADD_NEW_COMPONENTS, bINIT.getSelection() ? "init" : "ctor");
		getPreferenceStore().setValue(REF_TYPES, bRefactorType.getSelection());
		getPreferenceStore().setValue(REF_HTML, bRefactorHTML.getSelection());
		getPreferenceStore().setValue(REF_FIELDS, bRefactorFields.getSelection());
		getPreferenceStore().setValue(EXCLUDES, txtExcludes.getText());
		getPreferenceStore().setValue(SEVERITY, cbSeverity.getText());

		checkRebuild();

		return super.performOk();
	}

	private void checkRebuild() {
		if (rebuildNeeded) {
			GlobalBuildAction action = new GlobalBuildAction(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), IncrementalProjectBuilder.FULL_BUILD);
			action.doBuild();
		}
	}

	public void init(final IWorkbench workbench) {
		setPreferenceStore(QWickieActivator.getDefault().getPreferenceStore());
		setMessage("QWickie settings");
		//		setDescription("");
	}

}
