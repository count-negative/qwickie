package org.qwickie.test.project.webapp;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;

/**
 * @author count.negative
 *
 */
public class FirstPage extends WebPage {

	private static final long serialVersionUID = 1L;

	public FirstPage() {
		add(new Label("theID"));
	}
}
