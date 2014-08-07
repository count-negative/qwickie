package org.qwickie.test.project.getstring;


import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;

public class GetString extends WebPage {
	private static final long serialVersionUID = 1L;

	public GetString() {
		final Form<Person> form = new Form<Person>("form", new CompoundPropertyModel<Person>(new Person()));
		add(form);
		form.add(newLabel("firstname"));
		form.add(new Label("lastname", new ResourceModel("lastname")));
	}

	/**
	 * @return
	 */
	private Component newLabel(final String wicketid) {
		return new TextField<String>(wicketid).setLabel(Model.of(getString("firstname")));
	}
}
