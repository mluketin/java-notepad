package hr.fer.zemris.ooup.lab3.edit;

import hr.fer.zemris.ooup.lab3.Location;
import hr.fer.zemris.ooup.lab3.LocationRange;
import hr.fer.zemris.ooup.lab3.TextEditorModel;

import java.util.ArrayList;
import java.util.List;


public class ClearDocumentAction implements EditAction {


    private TextEditorModel model;

    //kopija stanja prije akcije
    private List<String> oldList;
    private LocationRange oldRange;
    private Location oldCursor;

    public ClearDocumentAction(TextEditorModel model) {
        oldList = new ArrayList<>(model.getLines());
        oldRange = LocationRange.copy(model.getSelectionRange());
        oldCursor = model.getCursorLocation().clone();

        this.model = model;
    }

    @Override
    public void execute_do() {
        model.setLines(new ArrayList<>());
        model.setCursorLocation(new Location(0, 0));
        model.setSelectionRange(null);
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
