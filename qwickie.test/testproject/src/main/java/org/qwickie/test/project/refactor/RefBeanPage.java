package org.qwickie.test.project.refactor;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;

public class RefBeanPage extends WebPage {
	private static final long serialVersionUID = 1L;

	PersonBean person = new PersonBean();

	public RefBeanPage() {
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		setDefaultModel(new CompoundPropertyModel<RefBeanPage>(this));

		final PersonBean personBean = new PersonBean();
		final Form<PersonBean> form = new Form<PersonBean>("form", new CompoundPropertyModel<PersonBean>(personBean));
		add(form);
		form.add(new TextField<String>("name"));
		form.add(new TextField<Integer>("dec"));

		add(new Label("person.name"));
	}
}
