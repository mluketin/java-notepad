package hr.fer.zemris.ooup.lab3.clipboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ClipboardStack {
    private Stack<String> texts;

    private List<ClipboardObserver> observers;

    public ClipboardStack() {
        texts = new Stack<>();
        observers = new ArrayList<>();
    }

    /**
     * Adds text on stack.
     *
     * @param text puts it on stack, if null nothing happens
     */
    public void pushText(String text) {
        if (text != null) {
            texts.push(text);
            notifyObservers();
        }
    }

    public String popText() {
        String text = texts.pop();
        notifyObservers();
        return text;
    }

    public String peekText() {
        return texts.peek();
    }

    public boolean isEmpty() {
        return texts.isEmpty();
    }

    public void deleteTexts() {
        texts.removeAllElements();
        notifyObservers();
    }

    public void attach(ClipboardObserver observer) {
        observers.add(observer);
    }

    public void dettach(ClipboardObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers() {
        for (ClipboardObserver observer : observers) {
            observer.updateClipboard();
        }
    }
}
