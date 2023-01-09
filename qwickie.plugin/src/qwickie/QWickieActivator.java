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
package qwickie;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import qwickie.listener.QWickiePartListener;
import qwickie.preferences.QWickiePreferencePage;
import qwickie.preferences.QWickiePreferencePage.SEVERITIES;

/**
 * The activator class controls the plug-in life cycle
 */
public class QWickieActivator extends AbstractUIPlugin implements IStartup {

	// The plug-in ID
	public static final String PLUGIN_ID = "qwickie";

	public static final String WICKET_ID = "wicket:id";
	public static final String WICKET_DTD = "http://wicket.apache.org";

	// The shared instance
	private static QWickieActivator plugin;
	private static final QWickiePartListener partListener = new QWickiePartListener();

	/**
	 * The constructor
	 */
	public QWickieActivator() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.
	 * BundleContext )
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.
	 * BundleContext )
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static QWickieActivator getDefault() {
		return plugin;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IStartup#earlyStartup()
	 */
	@Override
	public void earlyStartup() {

		getPreferenceStore().setDefault(QWickiePreferencePage.ADD_NEW_COMPONENTS, "init");
		getPreferenceStore().setDefault(QWickiePreferencePage.OPEN_HTML_FILES, false);
		getPreferenceStore().setDefault(QWickiePreferencePage.OPEN_PROPERTIES_FILES, false);
		getPreferenceStore().setDefault(QWickiePreferencePage.REF_TYPES, true);
		getPreferenceStore().setDefault(QWickiePreferencePage.REF_HTML, true);
		getPreferenceStore().setDefault(QWickiePreferencePage.REF_FIELDS, false);
		getPreferenceStore().setDefault(QWickiePreferencePage.SEVERITY, SEVERITIES.error.name());
		getPreferenceStore().setDefault(QWickiePreferencePage.EXCLUDES, "target");

		if (openAnyWicketFiles()) {
			addPartListener();
		}
	}

	/**
	 * if any corresponding wicket files (html/properties) should be opened when
	 * opening a java file
	 *
	 * @return true if any of them should (html/properties) be opened
	 */
	public boolean openAnyWicketFiles() {
		return openHTMLFiles() || openPropertiesFiles();
	}

	/**
	 * @return true if properties files should be opened when opening a java file
	 */
	public boolean openPropertiesFiles() {
		return Boolean.TRUE.equals(getPreferenceStore().getBoolean(QWickiePreferencePage.OPEN_PROPERTIES_FILES));
	}

	/**
	 * @return true if html files should be opened when opening a java file
	 */
	public boolean openHTMLFiles() {
		return Boolean.TRUE.equals(getPreferenceStore().getBoolean(QWickiePreferencePage.OPEN_HTML_FILES));
	}

	public void addPartListener() {
		// add Listener to get events when open/closing files in editor
		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(() -> {
			final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
			if (window != null) {
				window.getActivePage().addPartListener(partListener);
			}
		});
	}

	public void removePartListener() {
		// add Listener to get events when open/closing files in editor
		final IWorkbench workbench = PlatformUI.getWorkbench();
		workbench.getDisplay().asyncExec(() -> {
			final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
			if (window != null) {
				window.getActivePage().removePartListener(partListener);
			}
		});
	}
}
