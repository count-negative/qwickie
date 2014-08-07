package qwickie.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.junit.Before;
import org.junit.Test;

public class QWickieHandlerTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testHandler() {
		long start = System.nanoTime();

		QWickieHandler handler = new QWickieHandler();
		ExecutionEvent event = new ExecutionEvent();
		//		event.getParameters().put(ISources.ACTIVE_EDITOR_NAME, PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor());
		//		try {
		//			handler.execute(event);
		//		} catch (ExecutionException e) {
		//		}

		System.out.println("testHandler:\t\t\t" + (System.nanoTime() - start));
	}

}
