package org.qwickie.test.project.issue44;

import java.util.Arrays;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class Issue22 extends WebPage {

	private static final long serialVersionUID = 1L;

	public Issue22(final PageParameters parameters) {

		add(new ListView<String>("lv", Arrays.asList("abc", "def")) {
			@Override
			protected void populateItem(final ListItem<String> item) {
				item.add(new Label("", item.getModelObject()));
			}
		});

	}
}
