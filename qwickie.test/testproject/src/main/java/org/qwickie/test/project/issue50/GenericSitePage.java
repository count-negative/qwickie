package org.qwickie.test.project.issue50;

import org.apache.wicket.markup.html.basic.Label;

public class GenericSitePage extends QuickStartPage {
        private static final long serialVersionUID = 1L;

        public GenericSitePage() {
            add(new Label("header", "header!"));
            add(new Label("menu", "menu!"));
            add(new Label("content", "content!"));
            add(new Label("footer", "footer!"));
    }
}
