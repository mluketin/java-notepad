package hr.fer.zemris.ooup.lab3;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Test {

    public static void main(String[] args) {
        JFrame testFrame = new JFrame();
        testFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        TextEditorModel textEditorModel = new TextEditorModel("".toCharArray());
        TextEditor textEditor = new TextEditor(textEditorModel);
        textEditor.setPreferredSize(new Dimension(500, 400));

        JPanel panel = new JPanel();
        panel.add(textEditor.getMenu(), BorderLayout.NORTH);
        panel.add(textEditor.getToolBar(), BorderLayout.SOUTH);

//	    testFrame.getContentPane().add(textEditor.getMenu(), BorderLayout.NORTH);
//	    testFrame.getContentPane().add(textEditor.getToolBar());
        testFrame.getContentPane().add(panel, BorderLayout.NORTH);

        testFrame.getContentPane().add(textEditor);
        testFrame.getContentPane().add(textEditor.getStatusBar(), BorderLayout.SOUTH);

        testFrame.pack();
        testFrame.setVisible(true);
        testFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }
}
