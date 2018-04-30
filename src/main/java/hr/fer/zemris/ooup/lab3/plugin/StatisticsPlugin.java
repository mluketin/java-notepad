package hr.fer.zemris.ooup.lab3.plugin;

import hr.fer.zemris.ooup.lab3.TextEditorModel;
import hr.fer.zemris.ooup.lab3.clipboard.ClipboardStack;
import hr.fer.zemris.ooup.lab3.edit.UndoManager;

import javax.swing.JOptionPane;


public class StatisticsPlugin implements Plugin {

    @Override
    public String getName() {
        return "Statistics";
    }

    @Override
    public String getDescription() {
        return "Opens dialog with statistics";
    }

    @Override
    public void execute(TextEditorModel model, UndoManager undoManager, ClipboardStack clipboardStack) {
        StringBuilder sb = new StringBuilder();
        sb.append("Number of lines: ").append(model.getLines().size()).append("\n");

        int letters = 0;
        int words = 0;

        String[] tmp = null;
        for (String line : model.getLines()) {
            tmp = line.split(" ");
            for (int i = 0; i < tmp.length; i++) {
                if (!tmp[i].isEmpty()) {
                    letters += tmp[i].length();
                    words++;
                }
            }
        }

        sb.append("Number of words: ").append(words).append("\n");
        sb.append("Number of letters: ").append(letters);

        JOptionPane.showMessageDialog(null, sb.toString(), "Statistics", JOptionPane.INFORMATION_MESSAGE);
    }
}
