package org.qwickie.test.project.panel;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;

public class FieldsPanel extends AbstractFieldsPanel {
	private static final long serialVersionUID = 1L;

	public FieldsPanel(final String id) {
		super(id);

		add(new Label("label"));
		add(new Label("labelo"));

		final Form f = new Form("form");
		add(f);
		f.add(new Label("a"));
		f.add(new TextField<String>("txt1"));
		f.add(new TextField("txt2"));
		f.add(new CheckBox("booleanProperty"));
		f.add(new DropDownChoice("sel"));
		f.add(new Button("submit"));
		f.add(newLabel(LE_STRING));

		f.add(newPanel("panel"));
		getWhat("panel");
		getString("panel");
	}

	/**
	 * @param pete
	 * @return
	 */
	private Label newLabel(final String pete) {
		return new Label(pete);
	}

	/**
	 * @param string
	 */
	private String getWhat(final String string) {
		return string;
	}

	/**
	 * @param string
	 * @return
	 */
	private Component newPanel(final String string) {
		return new CustomerPanel(string);
	}


}
