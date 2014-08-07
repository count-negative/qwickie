package org.qwickie.test.project.issue49;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class Issue49 extends WebPage {

	private static final long serialVersionUID = 1L;

	public Issue49(final PageParameters parameters) {

		add( new Label("a") );
		add( new Label( "b" ) );
		add( new Label("c" ) );
		add( new Label("c" ) );
		add( new Label( "d") );
		
		add( new Label( "h" , Model.of("Test")) );
		
		add( new TextField<Integer>( "input1" , new Model<Integer>() ) );
		add( new TextField<Integer>("input2", new Model<Integer>() ) );
		add( new TextField<Integer>( "input3", new Model<Integer>() ) );
	}
}
