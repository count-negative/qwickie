package org.qwickie.test.project.refactor;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

public class ModalPanel2 extends Panel {

	private AjaxLink<Void> link;
	public ModalPanel2(String id) {
		super(id);
		link = new AjaxLink<Void>("link") {

			private static final long serialVersionUID = -3279080806930742579L;

			@Override
			public void onClick(AjaxRequestTarget target) {
			}

		};
		add(new Label("content", "2222"));
	}
}
