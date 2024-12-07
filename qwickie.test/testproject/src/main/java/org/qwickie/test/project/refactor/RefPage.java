package org.qwickie.test.project.refactor;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

public class RefPage extends WebPage {
	private static final long serialVersionUID = 1L;

	public RefPage(final PageParameters parameters) {
		super(parameters);

		final ModalWindow modal;
		add(modal = new ModalWindow("modal"));
		final ModalPanel modalPanel = new ModalPanel(modal.getContentId());
		modal.setContent(modalPanel);

		add(new AjaxLink<Void>("link") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {
				AjaxLink<Void> ajaxLink = new AjaxLink<Void>("content"){

					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						ModalPanel2 modalPanel2 = new ModalPanel2("content");
						modalPanel.replace(modalPanel2);	
						target.add(modalPanel2);				
					}
					
				};
				add(ajaxLink.setOutputMarkupPlaceholderTag(true));
				modalPanel.add.replaceWith(ajaxLink);	
				modal.show(target);
			}
		});
	}
}
