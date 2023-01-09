package org.qwickie.test.project.bug14;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class Bug14Page extends WebPage {

	private static final long serialVersionUID = 1L;

	public Bug14Page(final PageParameters parameters) {
		record Foo (String bar) {}
		
		final List<Foo> list = new ArrayList<Foo>();
		final PageableListView<Foo> plv = new PageableListView<>("plv", list, 10) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final ListItem<Foo> item) {
				// TODO Auto-generated method stub
				add(new Label("test"));

			}};
			add(plv);
			
			add(new PagingNavigation("navigator_top", plv));
	}
}
