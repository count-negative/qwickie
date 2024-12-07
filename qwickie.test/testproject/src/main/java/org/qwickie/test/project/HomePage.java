package org.qwickie.test.project;

import java.util.Date;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.WebPage;

public class HomePage extends WebPage {
	private static final long serialVersionUID = 1L;

    public HomePage(final PageParameters parameters) {
		add(new Label("customer"));
		add(new Label("message", getString("some.message")));
		add(new Label("message2", "message 2"));
		add(new Label("testMessageInXML", getString("testMessageInXML")));
		add(new Label("date", new Date().toString()));
    }
    
    @Override
    protected void onInitialize() {
    	super.onInitialize();
    }
}
