package hr.fer.zemris.ooup.lab3;

import hr.fer.zemris.ooup.lab3.edit.*;
import hr.fer.zemris.ooup.lab3.observer.CursorObserver;
import hr.fer.zemris.ooup.lab3.observer.TextObserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class TextEditorModel {

    private List<String> lines;
    private LocationRange selectionRange;
    private Location cursorLocation; // koordinate znaka ispred kojeg je kursor
    // npr (0,0) znaci da je kursor ispred
    // prvog znaka

    private List<CursorObserver> cursorObservers;
    private List<TextObserver> textObservers;

    private UndoManager undoManager;

    public TextEditorModel(char[] charArray) {
        undoManager = UndoManager.getInstance();
        cursorObservers = new ArrayList<>();
        textObservers = new ArrayList<>();
        initLines(charArray);

        cursorLocation = new Location(0, 0); // poc vrijednost kursora je na
        // pocetku, prije prvog znaka
    }

    private void initLines(char[] charArray) {
        lines = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (char c : charArray) {

            if (c == '\n') {
                lines.add(sb.toString());
                sb = new StringBuilder();
            } else {
                sb.append(String.valueOf(c));
            }
        }
        if (sb.length() > 0) {
            lines.add(sb.toString());
        }
    }

    public void createNewDocument(char[] chArray) {
        initLines(chArray);
        cursorLocation.setColumn(0);
        cursorLocation.setRow(0);
        selectionRange = null;
        notifyCursorObservers();
        notifyTextObservers();
    }

    public LocationRange getSelectionRange() {
        return selectionRange;
    }

    public void setSelectionRange(LocationRange range) {
        this.selectionRange = range;

    }

    public Location getCursorLocation() {
        return cursorLocation;
    }

    public void setCursorLocation(Location cursorLocation) {
        this.cursorLocation = cursorLocation;
        notifyCursorObservers();
    }

    public List<String> getLines() {
        return lines;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
        notifyTextObservers();
        notifyCursorObservers();
    }

    public Iterator<String> allLines() {
        return lines.iterator();
    }

    /**
     * [index1, index2)
     *
     * @param index1
     * @param index2
     * @return
     */
    public Iterator<String> linesRange(int index1, int index2) {
        return lines.subList(index1, index2).iterator();
    }

    /**
     * @return selected text or null if there is no selection
     */
    public String getSelectedText() {
        if (selectionRange == null)
            return null;

        if (selectionRange.getBegin().getRow() == selectionRange.getEnd().getRow()) {
            return lines.get(selectionRange.getBegin().getRow()).substring(selectionRange.getBegin().getColumn(),
                    selectionRange.getEnd().getColumn());
        }

        StringBuilder sb = new StringBuilder();
        for (int i = selectionRange.getBegin().getRow(); i <= selectionRange.getEnd().getRow(); i++) {
            String line = lines.get(i);

            if (i == selectionRange.getBegin().getRow()) {
                sb.append(line.substring(selectionRange.getBegin().getColumn(), line.length())).append("\n");
            } else if (i == selectionRange.getEnd().getRow()) {
                sb.append(line.substring(0, selectionRange.getEnd().getColumn()));
            } else {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    public void attachCursorObserver(CursorObserver observer) {
        cursorObservers.add(observer);
    }

    public void dettachCursorObserver(CursorObserver observer) {
        cursorObservers.remove(observer);
    }

    public void notifyCursorObservers() {
        for (CursorObserver observer : cursorObservers) {
            observer.updateCursorLocation(cursorLocation);
        }
    }

    /**
     * Moving location to the left in current line If locatio is on left edge,
     * moving to upper right location
     *
     * @param location
     * @return
     */
    public boolean moveLocationLeft(Location location) {
        if (location.getColumn() != 0) {
            location.setColumn(location.getColumn() - 1);
            return true;
        } else {
            if (location.getRow() != 0) {
                location.setRow(location.getRow() - 1);
                location.setColumn(lines.get(location.getRow()).length());
                return true;
            }
        }
        return false;
    }

    public boolean moveLocationRight(Location location) {
        if (location.getColumn() != lines.get(location.getRow()).length()) {
            location.setColumn(location.getColumn() + 1);
            return true;
        } else {
            if (location.getRow() != lines.size() - 1) {
                location.setRow(location.getRow() + 1);
                location.setColumn(0);
                return true;
            }
        }
        return false;
    }

    public boolean moveLocationUp(Location location) {
        if (location.getRow() != 0) {
            location.setRow(location.getRow() - 1);
            if (lines.get(location.getRow()).length() < location.getColumn()) {
                location.setColumn(lines.get(location.getRow()).length());
            }
            return true;
        } else {
            location.setColumn(0);
        }
        return false;
    }

    public boolean moveLocationDown(Location location) {
        if (location.getRow() < lines.size() - 1) {
            location.setRow(location.getRow() + 1);
            if (lines.get(location.getRow()).length() < location.getColumn()) {
                location.setColumn(lines.get(location.getRow()).length());
            }
            return true;
        } else {
            location.setColumn(lines.get(location.getRow()).length());
        }
        return false;
    }

    public void moveCursorLeft() {
        if (selectionRange != null) {
            cursorLocation = selectionRange.getBegin();
            notifyCursorObservers();
        } else {
            if (moveLocationLeft(cursorLocation)) {
                notifyCursorObservers();
            }
        }
    }

    public void moveCursorRight() {
        if (selectionRange != null) {
            cursorLocation = selectionRange.getEnd();
            notifyCursorObservers();
        } else {
            if (moveLocationRight(cursorLocation)) {
                notifyCursorObservers();
            }
        }
    }

    public void moveCursorUp() {
        if (moveLocationUp(cursorLocation)) {
            notifyCursorObservers();
        }
    }

    public void moveCursorDown() {
        if (moveLocationDown(cursorLocation)) {
            notifyCursorObservers();
        }
    }

    public void moveCursorToStart() {
        cursorLocation.setRow(0);
        cursorLocation.setColumn(0);
        notifyCursorObservers();
    }

    public void moveCursorToEnd() {
        cursorLocation.setRow(lines.size() - 1);
        cursorLocation.setColumn(lines.get(cursorLocation.getRow()).length());
        notifyCursorObservers();
    }


    public void selectionLeft() {
        setDefaultRange();

        Location begin = selectionRange.getBegin();
        Location end = selectionRange.getEnd();

        if (begin.equals(cursorLocation)) {
            if (moveLocationLeft(begin)) {
                selectionRange.setBegin(begin);
                cursorLocation = begin;
            }
        } else if (end.equals(cursorLocation)) {
            if (moveLocationLeft(end)) {
                selectionRange.setEnd(end);
                cursorLocation = end;
            }
        }
        notifyCursorObservers();
    }

    public void selectionRight() {
        setDefaultRange();

        Location begin = selectionRange.getBegin();
        Location end = selectionRange.getEnd();

        if (end.equals(cursorLocation)) {
            if (moveLocationRight(end)) {
                selectionRange.setEnd(end);
                cursorLocation = end;
                System.out.println(selectionRange.getEnd().toString());
                System.out.println(cursorLocation.toString());
            }
        } else if (begin.equals(cursorLocation)) {
            if (moveLocationRight(begin)) {
                selectionRange.setBegin(begin);
                cursorLocation = begin;
            }
        }
        notifyCursorObservers();
    }

    public void selectionUp() {
        setDefaultRange();

        Location begin = selectionRange.getBegin();
        Location end = selectionRange.getEnd();

        if (begin.equals(cursorLocation)) {
            if (moveLocationUp(begin)) {
                selectionRange.setBegin(begin);
                cursorLocation = begin;
            }
        } else if (end.equals(cursorLocation)) {
            if (moveLocationUp(end)) {
                if (begin.isLesserThan(end)) {
                    selectionRange.setBegin(begin);
                    selectionRange.setEnd(end);
                } else {
                    selectionRange.setBegin(end);
                    selectionRange.setEnd(begin);
                }
                cursorLocation = end;
            }
        }
        notifyCursorObservers();
    }

    public void selectionDown() {
        setDefaultRange();

        Location begin = selectionRange.getBegin();
        Location end = selectionRange.getEnd();

        if (end.equals(cursorLocation)) {
            if (moveLocationDown(end)) {
                selectionRange.setEnd(end);
                cursorLocation = end;
            }
        } else if (begin.equals(cursorLocation)) {
            if (moveLocationDown(begin)) {
                if (begin.isLesserThan(end)) {
                    selectionRange.setBegin(begin);
                    selectionRange.setEnd(end);
                } else {
                    selectionRange.setBegin(end);
                    selectionRange.setEnd(begin);
                }
                cursorLocation = begin;
            }

        }
        notifyCursorObservers();
    }

    /**
     * Sets range begin and end to cursor position if selection range is null
     */
    private void setDefaultRange() {
        if (selectionRange == null) {
            selectionRange = new LocationRange(cursorLocation.clone(), cursorLocation.clone());
        }
    }

    public void attachTextObserver(TextObserver observer) {
        textObservers.add(observer);
    }

    public void dettachTextObserver(TextObserver observer) {
        textObservers.remove(observer);
    }

    public void notifyTextObservers() {
        for (TextObserver observer : textObservers) {
            observer.updateText();
        }
    }

    // backspace
    public void deleteBefore() {
        EditAction eleteBeforeAction = new DeleteBeforeAction(this);
        eleteBeforeAction.execute_do();
        undoManager.push(eleteBeforeAction);
//		if (cursorLocation.getColumn() != 0) {
//			String line = lines.get(cursorLocation.getRow());
//			if (moveLocationLeft(cursorLocation)) {
//				String firstPart = line.substring(0, cursorLocation.getColumn());
//				String secondPart = "";
//				if (cursorLocation.getColumn() < line.length() - 1) {
//					secondPart = line.substring(cursorLocation.getColumn() + 1, line.length());
//				}
//
//				String newLine = firstPart + secondPart;
//				lines.remove(line);
//				lines.add(cursorLocation.getRow(), newLine);
//			}
//		} else {
//			if (cursorLocation.getRow() != 0) {
//				String firstLine = lines.get(cursorLocation.getRow() - 1);
//				String secondLine = lines.get(cursorLocation.getRow());
//				String newLine = firstLine + secondLine;
//
//				lines.remove(cursorLocation.getRow());
//				lines.remove(cursorLocation.getRow() - 1);
//				cursorLocation.setRow(cursorLocation.getRow() - 1);
//				cursorLocation.setColumn(firstLine.length());
//
//				lines.add(cursorLocation.getRow(), newLine);
//			}
//		}
//
//		notifyCursorObservers();
//		notifyTextObservers();
    }

    // tipka delete
    public void deleteAfter() {
        EditAction deleteAfterAction = new DeleteAfterAction(this);
        deleteAfterAction.execute_do();
        undoManager.push(deleteAfterAction);
//		String line = lines.get(cursorLocation.getRow());
//		if (cursorLocation.getColumn() < line.length()) {
//			String firstPart = line.substring(0, cursorLocation.getColumn());
//			String secondPart = line.substring(cursorLocation.getColumn() + 1, line.length());
//			String newLine = firstPart + secondPart;
//			lines.remove(line);
//			lines.add(cursorLocation.getRow(), newLine);
//
//		} else {
//			if (cursorLocation.getColumn() == line.length()) {
//				if (cursorLocation.getRow() < lines.size() - 1) {
//					String firstLine = lines.get(cursorLocation.getRow());
//					String secondLine = lines.get(cursorLocation.getRow() + 1);
//					String newLine = firstLine + secondLine;
//
//					lines.remove(cursorLocation.getRow() + 1);
//					lines.remove(cursorLocation.getRow());
//					lines.add(cursorLocation.getRow(), newLine);
//				}
//			}
//		}
//		notifyTextObservers();
    }

    /**
     * @param r range to delete, if null nothing happens
     */
    public void deleteRange(LocationRange r) {
        EditAction deleteRangeAction = new DeleteRangeAction(this);
        deleteRangeAction.execute_do();
        undoManager.push(deleteRangeAction);
//		if (r != null) {
//
//			Location begin = r.getBegin();
//			Location end = r.getEnd();
//			cursorLocation = begin.clone();
//
//			if (begin.getRow() == end.getRow()) {
//				int row = begin.getRow();
//				String line = lines.get(row);
//				String partOne = line.substring(0, begin.getColumn());
//				String partTwo = line.substring(end.getColumn(), line.length());
//
//				String newLine = partOne + partTwo;
//
//				lines.remove(row);
//				lines.add(row, newLine);
//			} else {
//
//				String beginLine = "";
//				String endLine = "";
//				for (int i = end.getRow(); i >= begin.getRow(); i--) {
//					if (i == end.getRow()) {
//						endLine = lines.get(i).substring(end.getColumn(), lines.get(i).length());
//						lines.remove(i);
//					} else if (i == begin.getRow()) {
//						beginLine = lines.get(i).substring(0, begin.getColumn()) + endLine;
//						lines.remove(i);
//						lines.add(i, beginLine);
//					} else {
//						lines.remove(i);
//					}
//				}
//			}
//			selectionRange = null;
//
//			notifyCursorObservers();
//			notifyTextObservers();
//		}
    }

    public void insert(char c) {
        EditAction insertAction = new InsertAction(this, c);
        insertAction.execute_do();
        undoManager.push(insertAction);
//		if (selectionRange != null) {
//			deleteRange(selectionRange);
//			selectionRange = null;
//		}
//
//		if(lines.size() == 0) {
//			lines.add(String.valueOf(c));
//			cursorLocation.setColumn(1);
//			notifyCursorObservers();
//			notifyTextObservers();
//			return;
//		}
//
//		String line = lines.get(cursorLocation.getRow());
//		if (c != '\n') {
//
//			String newLine = line.substring(0, cursorLocation.getColumn()) + String.valueOf(c)
//					+ line.substring(cursorLocation.getColumn(), line.length());
//			lines.remove(cursorLocation.getRow());
//			lines.add(cursorLocation.getRow(), newLine);
//			cursorLocation.setColumn(cursorLocation.getColumn() + 1);
//
//		} else {
//
//			String firstLine = line.substring(0, cursorLocation.getColumn());
//			String secondLine = line.substring(cursorLocation.getColumn(), line.length());
//
//			lines.remove(cursorLocation.getRow());
//			lines.add(cursorLocation.getRow(), firstLine);
//			lines.add(cursorLocation.getRow() + 1, secondLine);
//
//			cursorLocation.setRow(cursorLocation.getRow() + 1);
//			cursorLocation.setColumn(0);
//		}
//
//		notifyCursorObservers();
//		notifyTextObservers();
    }

    public void insert(String text) {
        EditAction insertAction = new InsertAction(this, text);
        insertAction.execute_do();
        undoManager.push(insertAction);
//		for (char c : text.toCharArray()) {
//			insert(c);
//		}
    }

    public void clearDocument() {

        EditAction clearDocumentAction = new ClearDocumentAction(this);
        clearDocumentAction.execute_do();
        undoManager.push(clearDocumentAction);

    }
}
