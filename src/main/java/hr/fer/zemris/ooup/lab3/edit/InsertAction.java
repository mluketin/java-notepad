package hr.fer.zemris.ooup.lab3.edit;

import hr.fer.zemris.ooup.lab3.Location;
import hr.fer.zemris.ooup.lab3.LocationRange;
import hr.fer.zemris.ooup.lab3.TextEditorModel;

import java.util.ArrayList;
import java.util.List;


//konkretna naredba, EditAction je Command
public class InsertAction implements EditAction {

    private TextEditorModel model;

    //kopija stanja prije akcije
    private List<String> oldList;
    private LocationRange oldRange;
    private Location oldCursor;

    //dvije vrste inserrt metode
    private String text;
    private char ch;

    public InsertAction(TextEditorModel textEditorModel, char c) {
        oldList = new ArrayList<>(textEditorModel.getLines());
        oldRange = LocationRange.copy(textEditorModel.getSelectionRange());
        oldCursor = textEditorModel.getCursorLocation().clone();

        model = textEditorModel;
        ch = c;
    }

    public InsertAction(TextEditorModel textEditorModel, String text) {
        oldList = new ArrayList<>(textEditorModel.getLines());
        oldRange = LocationRange.copy(textEditorModel.getSelectionRange());
        oldCursor = textEditorModel.getCursorLocation().clone();

        model = textEditorModel;
        this.text = text;
    }

    @Override
    public void execute_do() {
        if (text == null) {
            insertChar(ch);
        } else {
            insertText(text);
        }
    }

    private void insertText(String text2) {
        for (char c : text2.toCharArray()) {
            insertChar(c);
        }
    }

    private void insertChar(char c) {
        LocationRange selectionRange = model.getSelectionRange();
        List<String> lines = model.getLines();
        Location cursorLocation = model.getCursorLocation();

        if (selectionRange != null) {
            model.deleteRange(selectionRange);
            selectionRange = null;
        }

        if (lines.size() == 0) {
            lines.add(String.valueOf(c));
            cursorLocation.setColumn(1);
            model.notifyCursorObservers();
            model.notifyTextObservers();
            return;
        }

        String line = lines.get(cursorLocation.getRow());
        if (c != '\n') {

            String newLine = line.substring(0, cursorLocation.getColumn()) + String.valueOf(c)
                    + line.substring(cursorLocation.getColumn(), line.length());
            lines.remove(cursorLocation.getRow());
            lines.add(cursorLocation.getRow(), newLine);
            cursorLocation.setColumn(cursorLocation.getColumn() + 1);

        } else {
            String firstLine = line.substring(0, cursorLocation.getColumn());
            String secondLine = line.substring(cursorLocation.getColumn(), line.length());

            lines.remove(cursorLocation.getRow());
            lines.add(cursorLocation.getRow(), firstLine);
            lines.add(cursorLocation.getRow() + 1, secondLine);

            cursorLocation.setRow(cursorLocation.getRow() + 1);
            cursorLocation.setColumn(0);
        }

        model.notifyCursorObservers();
        model.notifyTextObservers();
    }

    @Override
    public void execute_undo() {
        model.setLines(oldList);
        model.setSelectionRange(oldRange);
        model.setCursorLocation(oldCursor);
        model.notifyCursorObservers();
        model.notifyTextObservers();
    }

}
