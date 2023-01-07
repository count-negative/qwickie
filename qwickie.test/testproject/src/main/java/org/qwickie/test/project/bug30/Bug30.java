package org.qwickie.test.project.bug30;

import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.IFilterStateLocator;
import org.apache.wicket.markup.html.WebPage;

public class Bug30 extends WebPage {

	private static final long serialVersionUID = 1L;

	public Bug30() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.wicket.Component#onInitialize()
	 */
	@Override
	protected void onInitialize() {
		super.onInitialize();

		final ISortableDataProvider<String, String> dp = null;
		final List<IColumn<String, String>> cols = null;
		final DefaultDataTable<String, String> dt = new DefaultDataTable<String, String>("table", cols, dp, 10);
		add(dt);

		final FilterForm<String> form = new FilterForm<String>("filter-form", (IFilterStateLocator<String>) dp) {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit() {
				dt.setCurrentPage(0);
			}
		};
		add(form);
		final IFilterStateLocator<String> sp = null;
		dt.addTopToolbar(new FilterToolbar(dt, form));
	}

}
