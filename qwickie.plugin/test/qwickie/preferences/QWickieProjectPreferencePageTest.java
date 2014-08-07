package qwickie.preferences;

import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;

public class QWickieProjectPreferencePageTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testPage() {
		long start = System.nanoTime();

		QWickieProjectPreferencePage page = new QWickieProjectPreferencePage();
		page.createContents(new Shell());

		System.out.println("testPage:\t\t\t" + (System.nanoTime() - start));
	}

}
