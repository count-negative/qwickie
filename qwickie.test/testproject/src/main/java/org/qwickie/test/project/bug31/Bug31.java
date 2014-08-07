package org.qwickie.test.project.bug31;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.markup.repeater.data.ListDataProvider;

public class Bug31 extends WebPage {

	private static final long serialVersionUID = 1L;

	public Bug31() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.wicket.Component#onInitialize()
	 */
	@Override
	protected void onInitialize() {
		super.onInitialize();

		final IDataProvider<String> dataProvider = new ListDataProvider<String>();
		final GridView<String> gridView = new GridView<String>("rows", dataProvider) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateEmptyItem(final Item<String> arg0) {
			}

			@Override
			protected void populateItem(final Item<String> arg0) {
			}
		};
		gridView.setColumns(3);
		gridView.setRows(10);
		gridView.setOutputMarkupId(true);
		add(gridView);
	}

}
