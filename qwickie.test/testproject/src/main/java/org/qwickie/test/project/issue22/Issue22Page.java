package org.qwickie.test.project.issue22;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class Issue22Page extends WebPage {

	private static final long serialVersionUID = 1L;

	public Issue22Page(final PageParameters parameters) {

		add(new Label("label", "TEST"));
	}
}
