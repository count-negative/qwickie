package org.qwickie.test.project.issue45.web;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

public class RegistrationPage extends WebPage {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.wicket.Component#onInitialize()
	 */
	@Override
	protected void onInitialize() {
		super.onInitialize();
		add(new Label("test"));
	}
}
