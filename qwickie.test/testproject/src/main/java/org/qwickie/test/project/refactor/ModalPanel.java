package org.qwickie.test.project.refactor;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;

public class ModalPanel extends Panel {
	public MarkupContainer add;

	private AjaxLink<Void> link;
	public ModalPanel(String id) {
		super(id);
		link = new AjaxLink<Void>("link") {

			private static final long serialVersionUID = -3279080806930742579L;

			@Override
			public void onClick(AjaxRequestTarget target) {
			}

		};
		add = add(new EmptyPanel("content").setOutputMarkupPlaceholderTag(true));

	}
}
