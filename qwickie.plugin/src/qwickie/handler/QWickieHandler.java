package qwickie.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import qwickie.hyperlink.QWickieHtmlHyperlinkDetector;
import qwickie.hyperlink.WicketHyperlink;
import qwickie.util.DocumentHelper;

/**
 * @author count.negative
 * 
 */
public class QWickieHandler extends AbstractHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditor(event);
		ITextEditor textEditor = null;
		if (editor instanceof ITextEditor) {
			textEditor = (ITextEditor) editor;
		} else {
			Object o = editor.getAdapter(ITextEditor.class);
			if (o != null) {
				textEditor = (ITextEditor) o;
			}
		}
		if (textEditor != null) {
			boolean isJavaFile = textEditor.getEditorInput().getName().endsWith(".java");
			boolean isHtmlFile = textEditor.getEditorInput().getName().endsWith(".html");
			IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());
			if (document != null /* && document instanceof IStructuredDocument */) {
				ITextSelection textSelection = getCurrentSelection(textEditor);
				if (!textSelection.isEmpty()) {
					IRegion wicketIdRegion = null;

					if (isJavaFile) {
						final IRegion[] wicketRegions = DocumentHelper.findWicketRegions(editor, document, textSelection.getOffset());
						if (wicketRegions == null) {
							return null;
						}
						wicketIdRegion = wicketRegions[0];
					} else if (isHtmlFile) {
						QWickieHtmlHyperlinkDetector detector = new QWickieHtmlHyperlinkDetector();
						wicketIdRegion = detector.findWicketId(document, textSelection.getOffset());
					}

					if (wicketIdRegion == null) {
						return null;
					}
					String wicketId;
					try {
						wicketId = document.get(wicketIdRegion.getOffset(), wicketIdRegion.getLength());
						WicketHyperlink wh = new WicketHyperlink(wicketIdRegion, wicketId, isHtmlFile ? "java" : "html");
						wh.openJavaFileOnly(isHtmlFile);
						wh.open();
					} catch (BadLocationException e) {
					}

				}
			}
		}
		return null;
	}

	private static ITextSelection getCurrentSelection(ITextEditor textEditor) {
		ISelectionProvider provider = textEditor.getSelectionProvider();
		if (provider != null) {
			ISelection selection = provider.getSelection();
			if (selection instanceof ITextSelection) {
				return (ITextSelection) selection;
			}
		}
		return TextSelection.emptySelection();
	}

}
