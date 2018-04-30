package hr.fer.zemris.ooup.lab3.plugin;


import hr.fer.zemris.ooup.lab3.TextEditorModel;
import hr.fer.zemris.ooup.lab3.clipboard.ClipboardStack;
import hr.fer.zemris.ooup.lab3.edit.UndoManager;

public interface Plugin {

    public String getName(); // name of plugin (for menu)

    public String getDescription(); // short plugin description

    public void execute(TextEditorModel model, UndoManager undoManager, ClipboardStack clipboardStack);

}
