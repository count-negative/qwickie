package org.qwickie.test.project.panel;

import org.apache.wicket.extensions.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponentPanel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.FormComponentFeedbackIndicator;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;

public class CustomerPanel extends FormComponentPanel<Customer> {
	private static final long serialVersionUID = 1L;

	/**
	 * @param id
	 */
	public CustomerPanel(final String id) {
		super(id);
	}

	public CustomerPanel(final String id, final IModel<Customer> customerModel) {
		super(id, new CompoundPropertyModel<Customer>(customerModel));

		try {
			Thread.sleep(100);
		} catch (final InterruptedException e) {
			e.printStackTrace();
		}
		final Form<Customer> form = new Form<Customer>(      "customer", customerModel);
		add(form);

		final TextField<String> firstname = new TextField<String>("firstname");
		form.add(firstname);
		firstname.setRequired(true);
		final FormComponentFeedbackIndicator firstnameIndicator = new FormComponentFeedbackIndicator("firstIndicator");
		firstnameIndicator.setIndicatorFor(firstname);
		form.add(firstnameIndicator);
		form.add(new TextField<String>("lastname"));
		form.add(new DateTextField("birthday", "dd.MM.yyyy' um 'hh:mm"));
	}

}
