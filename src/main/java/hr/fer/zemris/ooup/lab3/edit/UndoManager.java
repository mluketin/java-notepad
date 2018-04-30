package hr.fer.zemris.ooup.lab3.edit;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class UndoManager {
    private static UndoManager instance;

    private Stack<EditAction> undoStack;
    private Stack<EditAction> redoStack;

    private List<StackObserver> undoObservers;
    private List<StackObserver> redoObservers;

    private UndoManager() {
        undoStack = new Stack<>();
        redoStack = new Stack<>();

        undoObservers = new ArrayList<>();
        redoObservers = new ArrayList<>();
    }

    public static UndoManager getInstance() {
        if (instance == null) {
            instance = new UndoManager();
        }
        return instance;
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            EditAction action = undoStack.pop();
            redoStack.push(action);
            action.execute_undo();

            notifyRedoObserversNotEmpty();
            if (undoStack.isEmpty()) {
                notifyUndoObserversEmpty();
            } else {
                notifyUndoObserversNotEmpty();
            }
        } else {
            notifyUndoObserversEmpty();
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {

            EditAction action = redoStack.pop();
            action.execute_do();
            undoStack.push(action);

            notifyUndoObserversNotEmpty();
            if (redoStack.isEmpty()) {
                notifyRedoObserversEmpty();
            } else {
                notifyRedoObserversNotEmpty();
            }

        } else {
            notifyRedoObserversEmpty();
        }
    }

    public void push(EditAction c) {
        redoStack.removeAllElements();
        undoStack.push(c);
        notifyRedoObserversEmpty();
        notifyUndoObserversNotEmpty();
    }

    public void attachUndoObserver(StackObserver observer) {
        undoObservers.add(observer);
    }

    public void dettachUndoObserver(StackObserver observer) {
        undoObservers.remove(observer);
    }

    private void notifyUndoObserversEmpty() {
        for (StackObserver stackObserver : undoObservers) {
            stackObserver.stackEmpty();
        }
    }

    private void notifyUndoObserversNotEmpty() {
        for (StackObserver stackObserver : undoObservers) {
            stackObserver.stackNotEmpty();
        }
    }

    public void attachRedoObserver(StackObserver observer) {
        redoObservers.add(observer);
    }

    public void dettachRedoObserver(StackObserver observer) {
        redoObservers.remove(observer);
    }

    private void notifyRedoObserversEmpty() {
        for (StackObserver stackObserver : redoObservers) {
            stackObserver.stackEmpty();
        }
    }

    private void notifyRedoObserversNotEmpty() {
        for (StackObserver stackObserver : redoObservers) {
            stackObserver.stackNotEmpty();
        }
    }

}
