package qwickie.listener;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.wst.sse.ui.StructuredTextEditor;

import qwickie.QWickieActivator;
import qwickie.hyperlink.WicketHyperlink;

@SuppressWarnings("restriction")
public class QWickiePartListener implements IPartListener {

	@Override
	public void partActivated(final IWorkbenchPart part) {
	}

	@Override
	public void partDeactivated(final IWorkbenchPart part) {
	}

	@Override
	public void partBroughtToTop(final IWorkbenchPart part) {
	}

	@Override
	public void partOpened(final IWorkbenchPart part) {
		if (part instanceof CompilationUnitEditor) {// JAVA
			final CompilationUnitEditor editor = (CompilationUnitEditor) part;
			final IResource openedResource = editor.getEditorInput().getAdapter(IResource.class);
			String extension = "";
			if (!QWickieActivator.getDefault().openHTMLFiles() && QWickieActivator.getDefault().openPropertiesFiles()) {
				extension = WicketHyperlink.PROPERTIES;
			}
			final List<String> filenamesToOpen = new WicketHyperlink(new Region(1, 1), "", WicketHyperlink.JAVA).getFilenamesToOpen(openedResource, extension);
			for (final String fileName : filenamesToOpen) {
				try {
					final IFile file = WicketHyperlink.getFile(fileName);
					if (file != null && file.exists()) {
						if ((QWickieActivator.getDefault().openHTMLFiles() && WicketHyperlink.HTML.equals(file.getFileExtension()))
								|| (QWickieActivator.getDefault().openPropertiesFiles() && WicketHyperlink.PROPERTIES.equals(file.getFileExtension()))) {
							IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
						}
					}
				} catch (final PartInitException e) {
				}
			}
		} else if (part instanceof StructuredTextEditor) { // HTML

		}
	}

	@Override
	public void partClosed(final IWorkbenchPart part) {
		if (part instanceof CompilationUnitEditor) {// JAVA
			if (!QWickieActivator.getDefault().openAnyWicketFiles()) {
				return;
			}
			final String className = ((CompilationUnitEditor) part).getPartName().split("\\.")[0];
			final IEditorSite editorSite = ((CompilationUnitEditor) part).getEditorSite();
			if (editorSite != null) {
				final IWorkbenchWindow workbenchWindow = editorSite.getWorkbenchWindow();
				if (workbenchWindow != null) {
					final IWorkbenchPage activePage = workbenchWindow.getActivePage();
					if (activePage != null) {
						final IEditorReference[] editorReferences = activePage.getEditorReferences();
						for (final IEditorReference editorReference : editorReferences) {
							final String partName = editorReference.getPartName();
							if (partName != null && partName.startsWith(className)) {
								part.getSite().getPage().closeEditor(editorReference.getEditor(false), true);
							}
						}
					}
				}
			}
		}
	}

}
