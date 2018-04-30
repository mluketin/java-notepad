package hr.fer.zemris.ooup.lab3;

import hr.fer.zemris.ooup.lab3.clipboard.ClipboardObserver;
import hr.fer.zemris.ooup.lab3.clipboard.ClipboardStack;
import hr.fer.zemris.ooup.lab3.edit.StackObserver;
import hr.fer.zemris.ooup.lab3.edit.UndoManager;
import hr.fer.zemris.ooup.lab3.observer.CursorObserver;
import hr.fer.zemris.ooup.lab3.observer.TextObserver;
import hr.fer.zemris.ooup.lab3.plugin.Plugin;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.Timer;


public class TextEditor extends JComponent implements CursorObserver, TextObserver {

    private TextEditorModel textEditorModel;
    private boolean drawCursor = true; // za blinkanje cursora

    private Font font = new Font("monospaced", Font.PLAIN, 12);

    private Location cursorLocation;
    private ClipboardStack clipboard;
    private UndoManager undoManager;

    private JMenuBar menuBar;
    private JToolBar toolBar;

    private List<Plugin> plugins;

    // file path (prilikom spremanja ako nije null se spremi na istu
    // lokaciju, inace se bira di ce spremit)
    private Path filePath = null;

    // Actions
    private OpenAction openAction = new OpenAction();
    private SaveAction saveAction = new SaveAction();
    private ExitAction exitAction = new ExitAction();

    private UndoAction undoAction = new UndoAction();
    private RedoAction redoAction = new RedoAction();
    private CutAction cutAction = new CutAction();
    private CopyAction copyAction = new CopyAction();
    private PasteAction pasteAction = new PasteAction();
    private PasteAndTakeAction pasteAndTakeAction = new PasteAndTakeAction();
    private DeleteSelectionAction deleteSelectionAction = new DeleteSelectionAction();
    private ClearDocumentAction clearDocumentAction = new ClearDocumentAction();

    private CursorToStartAction cursorToStartAction = new CursorToStartAction();
    private CursorToEndAction cursorToEndAction = new CursorToEndAction();

    private StatusBar statusBar;

    public TextEditor(TextEditorModel textEditorModel) {
        this.textEditorModel = textEditorModel;
        textEditorModel.attachCursorObserver(this);
        textEditorModel.attachTextObserver(this);
        cursorLocation = textEditorModel.getCursorLocation();

        this.setFocusable(true);
        clipboard = new ClipboardStack();
        undoManager = UndoManager.getInstance();
        statusBar = new StatusBar();
        plugins = new ArrayList<>();
        createActions();
        initMenu();
        initToolbar();
        loadPlugins();

        addPlugins();
        setListeners();
        addCursorTimer(); // svako nekoliko vremena repainta editor �to kursor
        // imitira blinkanje
    }

    private void loadPlugins() {
        String pluginDirPath = "./target/classes/hr/fer/zemris/ooup/lab3/plugin";
        File pluginDir = new File(pluginDirPath);

        try {
            ClassLoader loader = TextEditor.class.getClassLoader();
            URLClassLoader newClassLoader = new URLClassLoader(new URL[]{pluginDir.toURI().toURL()}, loader);

            File[] files = pluginDir.listFiles();
            for (File file : files) {

                String fileName = file.getName().substring(0, file.getName().lastIndexOf('.'));
                Class<Plugin> clazz = (Class<Plugin>) newClassLoader.loadClass("hr.fer.zemris.ooup.lab3.plugin." + fileName);

                Constructor<?> ctr = clazz.getConstructor(); //throws exception
                Plugin plugin = (Plugin) ctr.newInstance();

                plugins.add(plugin);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createNewTextEditorModel(String text) {
        this.textEditorModel.createNewDocument(text.toCharArray());
        cursorLocation.setColumn(0);
        cursorLocation.setRow(0);
    }

    public JMenuBar getMenu() {
        return menuBar;
    }

    public JToolBar getToolBar() {
        return toolBar;
    }

    public JLabel getStatusBar() {
        return statusBar;
    }

    private void createActions() {
        openAction.putValue(Action.NAME, "Open");
        saveAction.putValue(Action.NAME, "Save");
        exitAction.putValue(Action.NAME, "Exit");

        undoAction.putValue(Action.NAME, "Undo");
        undoAction.setEnabled(false);
        undoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Z"));
        undoAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_Z);
        undoManager.attachUndoObserver(undoAction);


        redoAction.putValue(Action.NAME, "Redo");
        redoAction.setEnabled(false);
        redoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Y"));
        redoAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_Y);
        undoManager.attachRedoObserver(redoAction);

        cutAction.putValue(Action.NAME, "Cut");
        cutAction.setEnabled(false);
        cutAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control X"));
        cutAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);

        copyAction.putValue(Action.NAME, "Copy");
        copyAction.setEnabled(false);
        copyAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control C"));
        copyAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);

        pasteAction.putValue(Action.NAME, "Paste");
        pasteAction.setEnabled(false);
        pasteAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control V"));
        pasteAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_V);
        clipboard.attach(pasteAction);

        pasteAndTakeAction.putValue(Action.NAME, "Paste and Take");
        pasteAndTakeAction.setEnabled(false);
        pasteAndTakeAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control shift V"));
        pasteAndTakeAction.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_V);
        clipboard.attach(pasteAndTakeAction);

        deleteSelectionAction.putValue(Action.NAME, "Delete Selection");

        clearDocumentAction.putValue(Action.NAME, "Clear Document");

        cursorToStartAction.putValue(Action.NAME, "Cursor to document start");

        cursorToEndAction.putValue(Action.NAME, "Cursor to document end");
    }

    private void initToolbar() {
        toolBar = new JToolBar();

        JButton undoButton = new JButton("Undo");
        JButton redoButton = new JButton("Redo");
        JButton cutButton = new JButton("Cut");
        JButton copyButton = new JButton("Copy");
        JButton pasteButton = new JButton("Paste");

        undoButton.setAction(undoAction);
        redoButton.setAction(redoAction);
        cutButton.setAction(cutAction);
        copyButton.setAction(copyAction);
        pasteButton.setAction(pasteAction);

        toolBar.add(undoButton);
        toolBar.add(redoButton);
        toolBar.add(cutButton);
        toolBar.add(copyButton);
        toolBar.add(pasteButton);
    }

    private void initMenu() {
        menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenu editMenu = new JMenu("Edit");
        JMenu moveMenu = new JMenu("Move");

        JMenuItem openItem = new JMenuItem(openAction);
        JMenuItem saveItem = new JMenuItem(saveAction);
        JMenuItem exitItem = new JMenuItem(exitAction);

        JMenuItem undoItem = new JMenuItem(undoAction);
        JMenuItem redoItem = new JMenuItem(redoAction);
        JMenuItem cutItem = new JMenuItem(cutAction);
        JMenuItem copyItem = new JMenuItem(copyAction);
        JMenuItem pasteItem = new JMenuItem(pasteAction);
        JMenuItem pasteAndTakeItem = new JMenuItem(pasteAndTakeAction);
        JMenuItem deleteSectionItem = new JMenuItem(deleteSelectionAction);
        JMenuItem clearDocumentItem = new JMenuItem(clearDocumentAction);

        JMenuItem cursorToStartItem = new JMenuItem(cursorToStartAction);
        JMenuItem cursorToEndItem = new JMenuItem(cursorToEndAction);

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(exitItem);

        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        editMenu.add(pasteAndTakeItem);
        editMenu.add(deleteSectionItem);
        editMenu.add(clearDocumentItem);

        moveMenu.add(cursorToStartItem);
        moveMenu.add(cursorToEndItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(moveMenu);
    }

    public void addPlugins() {
        JMenu pluginsMenu = new JMenu("Plugins");
        for (Plugin plugin : plugins) {

            Action a = new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    plugin.execute(textEditorModel, undoManager, clipboard);
                }
            };

            a.putValue(Action.NAME, plugin.getName());
            a.putValue(Action.SHORT_DESCRIPTION, plugin.getDescription());

            pluginsMenu.add(new JMenuItem(a));
        }
        menuBar.add(pluginsMenu);
    }

    private void addCursorTimer() {
        Timer timer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TextEditor.this.repaint();

            }
        });
        timer.start();
    }

    private void setListeners() {
        this.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        if (e.isShiftDown()) {
                            textEditorModel.selectionLeft();
                        } else {
                            textEditorModel.moveCursorLeft();
                            textEditorModel.setSelectionRange(null);
                        }
                        break;

                    case KeyEvent.VK_RIGHT:
                        if (e.isShiftDown()) {
                            textEditorModel.selectionRight();
                        } else {
                            textEditorModel.moveCursorRight();
                            textEditorModel.setSelectionRange(null);
                        }
                        break;

                    case KeyEvent.VK_UP:
                        if (e.isShiftDown()) {
                            textEditorModel.selectionUp();
                        } else {
                            textEditorModel.moveCursorUp();
                            textEditorModel.setSelectionRange(null);
                        }
                        break;

                    case KeyEvent.VK_DOWN:
                        if (e.isShiftDown()) {
                            textEditorModel.selectionDown();
                        } else {
                            textEditorModel.moveCursorDown();
                            textEditorModel.setSelectionRange(null);
                        }
                        break;

                    case KeyEvent.VK_DELETE:
                        if (textEditorModel.getSelectionRange() != null) {
                            textEditorModel.deleteRange(textEditorModel.getSelectionRange());
                            textEditorModel.setSelectionRange(null);
                        } else {
                            textEditorModel.deleteAfter();
                        }
                        break;

                    case KeyEvent.VK_BACK_SPACE:
                        if (textEditorModel.getSelectionRange() != null) {
                            textEditorModel.deleteRange(textEditorModel.getSelectionRange());
                            textEditorModel.setSelectionRange(null);
                        } else {
                            textEditorModel.deleteBefore();
                        }
                        break;

                    default:
                        if (!e.isControlDown() && !e.isActionKey() && !e.isMetaDown() && e.getKeyCode() != KeyEvent.VK_ALT
                                && e.getKeyCode() != KeyEvent.VK_SHIFT) {
                            textEditorModel.insert(e.getKeyChar());
                        }
                        // ZAKOMENTIRANO, AKCIJAMA SU DANI LISTENERI NA KEYSTROKE
                        // TAKO DA OVO DOLJE NETREBA JER CE AKCIJE IZVODI COPY I
                        // SLICNO

                        // else {
                        // if(e.isControlDown() &&
                        // !e.isActionKey() &&
                        // !e.isMetaDown() &&
                        // e.getKeyCode() != KeyEvent.VK_ALT &&
                        // e.getKeyCode() != KeyEvent.VK_SHIFT
                        // ) {
                        // if(textEditorModel.getSelectionRange() != null &&
                        // e.getKeyCode() == KeyEvent.VK_C) {
                        // String selectedText = textEditorModel.getSelectedText();
                        // clipboard.pushText(selectedText);
                        // System.out.println(selectedText);
                        //
                        // }
                        //
                        // }
                        //
                        // }
                        break;
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {

        // postavljanje enabled na akcijama
        if (textEditorModel.getSelectionRange() != null) {
            cutAction.setEnabled(true);
            copyAction.setEnabled(true);
            deleteSelectionAction.setEnabled(true);
        } else {
            cutAction.setEnabled(false);
            copyAction.setEnabled(false);
            deleteSelectionAction.setEnabled(false);
        }

        if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(Color.BLACK);
            // g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            // RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
            g2.setFont(font);

            // start position for drawing text
            int x = 5;
            int y = 15;

            LocationRange range = textEditorModel.getSelectionRange();

            // // Draw TEXT
            Iterator<String> iterator = textEditorModel.allLines();
            if (range != null) {
                Location begin = range.getBegin();
                Location end = range.getEnd();

                for (int row = 0; iterator.hasNext(); row++) {
                    String line = iterator.next();

                    if (row == begin.getRow() && row == end.getRow()) {
                        String firstPart = line.substring(0, begin.getColumn());
                        String secondPart = line.substring(begin.getColumn(), end.getColumn());
                        String thirdPart = line.substring(end.getColumn(), line.length());

                        // draw first part of string (black)
                        g2.drawString(firstPart, x, y);

                        // draw blue rectangle
                        g2.setColor(Color.LIGHT_GRAY);

                        int rectx1 = x + g.getFontMetrics().stringWidth(firstPart);
                        int rectx2 = rectx1 + g.getFontMetrics().stringWidth(secondPart);
                        int rectWidth = g.getFontMetrics().stringWidth(secondPart);
                        int rectHeight = 15;
                        g2.fillRect(rectx1, y - 10, rectWidth + 1, rectHeight);

                        // draw second part of string (white)
                        g2.setColor(Color.WHITE);
                        g2.drawString(secondPart, rectx1, y);

                        // draw third part of string (black)
                        g2.setColor(Color.BLACK);
                        g2.drawString(thirdPart, rectx2, y);

                        // koordinate plavog pravokutnika
                    } else {

                        if (row == begin.getRow()) {

                            String firstPart = line.substring(0, begin.getColumn());
                            String secondPart = line.substring(begin.getColumn(), line.length()) + " ";

                            // draw first part of string (black)
                            g2.drawString(firstPart, x, y);

                            // draw blue rectangle
                            g2.setBackground(Color.LIGHT_GRAY);
                            g2.setColor(Color.LIGHT_GRAY);

                            int rectx1 = x + g.getFontMetrics().stringWidth(firstPart);
                            int rectx2 = rectx1 + g.getFontMetrics().stringWidth(secondPart);
                            int rectWidth = g.getFontMetrics().stringWidth(secondPart);
                            int rectHeight = 15;
                            g2.fillRect(rectx1, y - 10, rectWidth, rectHeight);

                            // draw second part of string (white)
                            g2.setColor(Color.WHITE);
                            g2.drawString(secondPart, rectx1, y);
                            g2.setColor(Color.BLACK);

                        } else if (row == end.getRow()) {
                            String firstPart = line.substring(0, end.getColumn());
                            String secondPart = line.substring(end.getColumn(), line.length());

                            // draw blue rectangle
                            g2.setBackground(Color.LIGHT_GRAY);
                            g2.setColor(Color.LIGHT_GRAY);

                            int rectx1 = x + g.getFontMetrics().stringWidth(firstPart);
                            int rectx2 = rectx1 + g.getFontMetrics().stringWidth(secondPart);
                            int rectWidth = g.getFontMetrics().stringWidth(firstPart);
                            int rectHeight = 15;
                            g2.fillRect(x, y - 10, rectWidth, rectHeight);

                            // draw second part of string (white)
                            g2.setColor(Color.WHITE);
                            g2.drawString(firstPart, x, y);
                            g2.setColor(Color.BLACK);

                            // draw first part of string (black)
                            g2.drawString(secondPart, x + rectWidth, y);

                        } else if (row > begin.getRow() && row < end.getRow()) {
                            line += " ";
                            g2.setBackground(Color.LIGHT_GRAY);
                            g2.setColor(Color.LIGHT_GRAY);
                            int rectWidth = g.getFontMetrics().stringWidth(line);
                            int rectHeight = 15;

                            g2.fillRect(x, y - 10, rectWidth, rectHeight);
                            // draw second part of string (white)
                            g2.setColor(Color.WHITE);
                            g2.drawString(line, x, y);
                            g2.setColor(Color.BLACK);

                        } else {
                            g2.drawString(line, x, y);
                        }
                    }

                    y += 15;
                }

            } else {

                // g2.setColor(Color.WHITE);
                while (iterator.hasNext()) {
                    g2.drawString(iterator.next(), x, y);
                    y += 15;

                }

            }

            // Draw CURSOR
            if (drawCursor) {
                int cursorX;
                if (textEditorModel.getLines().size() != 0) {
                    String cursorLine = textEditorModel.getLines().get(cursorLocation.getRow());
                    cursorX = x + g.getFontMetrics().stringWidth(cursorLine.substring(0, cursorLocation.getColumn()));
                } else {
                    cursorX = x;
                }

                y = 15 + cursorLocation.getRow() * 15;
                g2.drawLine(cursorX, y + 3, cursorX, y - 10);
                g.setColor(Color.BLACK);
                drawCursor = false;
            } else {
                drawCursor = true;
            }
        }
    }

    @Override
    public void updateCursorLocation(Location loc) {
        TextEditor.this.grabFocus();
        cursorLocation = loc;
        this.repaint();
    }

    @Override
    public void updateText() {
        TextEditor.this.grabFocus();
        this.repaint();
    }

    // AKCIJE ZA MENU
    private class OpenAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            final JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Open");

            if (fileChooser.showOpenDialog(TextEditor.this) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File file = fileChooser.getSelectedFile();
            Path path = file.toPath();

            if (!Files.isReadable(path)) {
                JOptionPane.showMessageDialog(TextEditor.this, "Could not open file " + file.getName(), "ERROR",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            byte[] bytes = null;
            try {
                bytes = Files.readAllBytes(path);
            } catch (IOException e1) {
                JOptionPane.showMessageDialog(TextEditor.this,
                        "While reading file " + file.getName() + ":" + e1.getMessage(), "ERROR",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            createNewTextEditorModel(new String(bytes, StandardCharsets.UTF_8));
            filePath = path;
            System.out.println(path);
        }
    }

    private class SaveAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (filePath == null) { // file ne postoji, moramo odredit lokaciju
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save file");
                if (fileChooser.showSaveDialog(TextEditor.this) != JFileChooser.APPROVE_OPTION) {
                    JOptionPane.showMessageDialog(TextEditor.this, "Changes are not saved!", "WARNING",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                filePath = fileChooser.getSelectedFile().toPath();
            }

            Iterator<String> iterator = textEditorModel.allLines();
            StringBuilder sb = new StringBuilder();
            while (iterator.hasNext()) {
                sb.append(iterator.next()).append("\n");
            }
            sb.deleteCharAt(sb.length() - 1); // brise zadnji dodani znak

            byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);

            try {
                Files.write(filePath, bytes);
            } catch (IOException exception) {
                JOptionPane.showMessageDialog(TextEditor.this,
                        "Error => " + filePath.toFile().getName() + ":" + exception.getMessage(), "ERROR",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(TextEditor.this, "File saved", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class ExitAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }

    private class UndoAction extends AbstractAction implements StackObserver {

        @Override
        public void actionPerformed(ActionEvent e) {
            undoManager.undo();
//			TextEditor.this.repaint();
        }

        @Override
        public void stackEmpty() {
            this.setEnabled(false);
        }

        @Override
        public void stackNotEmpty() {
            this.setEnabled(true);
        }
    }

    private class RedoAction extends AbstractAction implements StackObserver {

        @Override
        public void actionPerformed(ActionEvent e) {
            undoManager.redo();
//			TextEditor.this.repaint();
        }

        @Override
        public void stackEmpty() {
            this.setEnabled(false);
        }

        @Override
        public void stackNotEmpty() {
            this.setEnabled(true);
        }
    }

    private class CutAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            clipboard.pushText(textEditorModel.getSelectedText());
            textEditorModel.deleteRange(textEditorModel.getSelectionRange());
        }
    }

    private class CopyAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            clipboard.pushText(textEditorModel.getSelectedText());
        }
    }

    private class PasteAction extends AbstractAction implements ClipboardObserver {

        @Override
        public void actionPerformed(ActionEvent e) {
            textEditorModel.insert(clipboard.peekText());
        }

        @Override
        public void updateClipboard() {
            if (clipboard.isEmpty()) {
                this.setEnabled(false);
            } else {
                this.setEnabled(true);
            }
        }
    }

    private class PasteAndTakeAction extends AbstractAction implements ClipboardObserver {

        @Override
        public void actionPerformed(ActionEvent e) {
            textEditorModel.insert(clipboard.popText());
        }

        @Override
        public void updateClipboard() {
            if (clipboard.isEmpty()) {
                this.setEnabled(false);
            } else {
                this.setEnabled(true);
            }
        }
    }

    private class DeleteSelectionAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            textEditorModel.deleteRange(textEditorModel.getSelectionRange());
        }
    }

    private class ClearDocumentAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            textEditorModel.clearDocument();
        }
    }

    private class CursorToStartAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            textEditorModel.moveCursorToStart();
        }
    }

    private class CursorToEndAction extends AbstractAction {

        @Override
        public void actionPerformed(ActionEvent e) {
            textEditorModel.moveCursorToEnd();
        }
    }

    private class StatusBar extends JLabel implements CursorObserver {

        public StatusBar() {
            super.setText(String.format("Cursor Postition: (%d, %d)     Number of lines: %d", cursorLocation.getRow(),
                    cursorLocation.getColumn(), textEditorModel.getLines().size()));
            textEditorModel.attachCursorObserver(this);
        }

        @Override
        public void updateCursorLocation(Location loc) {
            super.setText(String.format("Cursor Postition: (%d, %d)     Number of lines: %d", loc.getRow(),
                    loc.getColumn(), textEditorModel.getLines().size()));
        }

    }
}
