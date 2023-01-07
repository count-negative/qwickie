package qwickie.wizard;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

public class QWickieNewPanelWizardTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testNewPanelWizard() {
		long start = System.nanoTime();

		QWickieNewPanelWizard wizard = new QWickieNewPanelWizard();
		Assert.assertEquals("Create a new Wicket Panel", wizard.getWindowTitle());

		System.out.println("testNewPanelWizard:\t\t" + (System.nanoTime() - start));
	}

}
