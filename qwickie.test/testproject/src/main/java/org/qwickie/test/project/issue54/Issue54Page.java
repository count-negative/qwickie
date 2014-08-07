package org.qwickie.test.project.issue54;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

/**
 * @author count.negative
 *
 */
public class Issue54Page extends WebPage {

	public Issue54Page() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.wicket.Component#onInitialize()
	 */
	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new Label("issue54Label", "test"));
	}

}
