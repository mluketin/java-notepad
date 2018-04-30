package hr.fer.zemris.ooup.lab3.edit;

import hr.fer.zemris.ooup.lab3.Location;
import hr.fer.zemris.ooup.lab3.LocationRange;
import hr.fer.zemris.ooup.lab3.TextEditorModel;

import java.util.ArrayList;
import java.util.List;


public class DeleteAfterAction implements EditAction {

    private TextEditorModel model;

    //kopija stanja prije akcije
    private List<String> oldList;
    private LocationRange oldRange;
    private Location oldCursor;

    public DeleteAfterAction(TextEditorModel model) {
        oldList = new ArrayList<>(model.getLines());
        oldRange = LocationRange.copy(model.getSelectionRange());
        oldCursor = model.getCursorLocation().clone();

        this.model = model;
    }

    @Override
    public void execute_do() {
        List<String> lines = model.getLines();
        Location cursorLocation = model.getCursorLocation();

        String line = lines.get(cursorLocation.getRow());
        if (cursorLocation.getColumn() < line.length()) {
            String firstPart = line.substring(0, cursorLocation.getColumn());
            String secondPart = line.substring(cursorLocation.getColumn() + 1, line.length());
            String newLine = firstPart + secondPart;
            lines.remove(line);
            lines.add(cursorLocation.getRow(), newLine);

        } else {
            if (cursorLocation.getColumn() == line.length()) {
                if (cursorLocation.getRow() < lines.size() - 1) {
                    String firstLine = lines.get(cursorLocation.getRow());
                    String secondLine = lines.get(cursorLocation.getRow() + 1);
                    String newLine = firstLine + secondLine;

                    lines.remove(cursorLocation.getRow() + 1);
                    lines.remove(cursorLocation.getRow());
                    lines.add(cursorLocation.getRow(), newLine);
                }
            }
        }
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
