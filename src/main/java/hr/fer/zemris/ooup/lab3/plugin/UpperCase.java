package hr.fer.zemris.ooup.lab3.plugin;

import hr.fer.zemris.ooup.lab3.TextEditorModel;
import hr.fer.zemris.ooup.lab3.clipboard.ClipboardStack;
import hr.fer.zemris.ooup.lab3.edit.UndoManager;

import java.util.ArrayList;
import java.util.List;


public class UpperCase implements Plugin {

    @Override
    public String getName() {
        return "UpperCase";
    }

    @Override
    public String getDescription() {
        return "Walks through document and sets each word's first letter to upper case.";
    }

    @Override
    public void execute(TextEditorModel model, UndoManager undoManager, ClipboardStack clipboardStack) {

        List<String> lines = model.getLines();
        List<String> newLines = new ArrayList<>();

        for (String line : lines) {
            char[] chArray = line.toCharArray();
            if (chArray.length > 0) {
                if (Character.isLetter(chArray[0])) {
                    chArray[0] = Character.toUpperCase(chArray[0]);
                }
            }
            line = String.valueOf(chArray);
            newLines.add(line);
        }
        model.setLines(newLines);
    }
}
