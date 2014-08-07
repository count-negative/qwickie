package org.qwickie.test.project.issue45.mobile;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

public class RegistrationPage extends WebPage {

	private static final long serialVersionUID = 1L;

	public RegistrationPage() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.wicket.Component#onInitialize()
	 */
	@Override
	protected void onInitialize() {
		super.onInitialize();
//		add(new Label("test"));
		add(new Label("test"));
	}
}
