package hr.fer.zemris.ooup.lab3.edit;

import hr.fer.zemris.ooup.lab3.Location;
import hr.fer.zemris.ooup.lab3.LocationRange;
import hr.fer.zemris.ooup.lab3.TextEditorModel;

import java.util.ArrayList;
import java.util.List;


public class DeleteRangeAction implements EditAction {

    private TextEditorModel model;

    //kopija stanja prije akcije
    private List<String> oldList;
    private LocationRange oldRange;
    private Location oldCursor;

    public DeleteRangeAction(TextEditorModel model) {
        oldList = new ArrayList<>(model.getLines());
        oldRange = LocationRange.copy(model.getSelectionRange());
        oldCursor = model.getCursorLocation().clone();

        this.model = model;
    }

    @Override
    public void execute_do() {
        LocationRange r = model.getSelectionRange();
        Location cursorLocation = model.getCursorLocation();
        List<String> lines = model.getLines();


        if (r != null) {

            Location begin = r.getBegin();
            Location end = r.getEnd();
            cursorLocation = begin.clone();

            if (begin.getRow() == end.getRow()) {
                int row = begin.getRow();
                String line = lines.get(row);
                String partOne = line.substring(0, begin.getColumn());
                String partTwo = line.substring(end.getColumn(), line.length());

                String newLine = partOne + partTwo;

                lines.remove(row);
                lines.add(row, newLine);
            } else {

                String beginLine = "";
                String endLine = "";
                for (int i = end.getRow(); i >= begin.getRow(); i--) {
                    if (i == end.getRow()) {
                        endLine = lines.get(i).substring(end.getColumn(), lines.get(i).length());
                        lines.remove(i);
                    } else if (i == begin.getRow()) {
                        beginLine = lines.get(i).substring(0, begin.getColumn()) + endLine;
                        lines.remove(i);
                        lines.add(i, beginLine);
                    } else {
                        lines.remove(i);
                    }
                }
            }
            model.setSelectionRange(null);

            model.notifyCursorObservers();
            model.notifyTextObservers();
        }
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
