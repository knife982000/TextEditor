package editor;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextEditor extends JFrame {
    public TextEditor() {
        List<Occurrence> occurrences = new ArrayList<>();
        AtomicInteger position = new AtomicInteger();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("The first stage");
        setSize(300, 300);
        getContentPane().setLayout(new BorderLayout());

        JTextArea jTextArea = new JTextArea();
        jTextArea.setName("TextArea");

        JScrollPane jScrollPane = new JScrollPane(jTextArea);
        jScrollPane.setName("ScrollPane");
        getContentPane().add(jScrollPane, BorderLayout.CENTER);
        jScrollPane.setBounds(40, 40, 400, 400);


        JPanel buttonPane = new JPanel();
        getContentPane().add(buttonPane, BorderLayout.NORTH);
        buttonPane.setLayout(new GridLayout());

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));
        fileChooser.setName("FileChooser");
        fileChooser.setVisible(false);
        add(fileChooser, BorderLayout.PAGE_END);

        /*JPanel hidden = new JPanel();
        fileChooser.setVisible(false);
        hidden.add(fileChooser);
        this.getContentPane().add(hidden, BorderLayout.PAGE_END);*/
        //add(fileChooser, BorderLayout.PAGE_END);

        JButton save = new JButton();
        save.setName("SaveButton");
        save.setText("Save");
        save.addActionListener( e -> {
            fileChooser.setVisible(true);
            try {
                int ret = fileChooser.showSaveDialog(null);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    BufferedWriter br = new BufferedWriter(new FileWriter(fileChooser.getSelectedFile()));
                    br.write(jTextArea.getText());
                    br.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            fileChooser.setVisible(false);
        });
        buttonPane.add(save);

        JButton load = new JButton();
        load.setName("OpenButton");
        load.setText("Load");
        load.addActionListener( e -> {
            fileChooser.setVisible(true);
            try {
                int ret = fileChooser.showOpenDialog(null);
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File f = fileChooser.getSelectedFile();
                    String text = Files.readString(f.toPath());
                    jTextArea.setText(text);
                }
            } catch (IOException exception) {
                jTextArea.setText("");
            }
            fileChooser.setVisible(false);
        });
        buttonPane.add(load);

        JCheckBox useRegExp = new JCheckBox();
        useRegExp.setName("UseRegExCheckbox");
        useRegExp.setText("Use RegExp");
        useRegExp.setSelected(false);

        JTextField searchField = new JTextField();
        searchField.setName("SearchField");
        buttonPane.add(searchField);

        JButton startSearch = new JButton();
        startSearch.setName("StartSearchButton");
        startSearch.setText("Start");
        startSearch.addActionListener( e -> {
            String exp = searchField.getText();
            if (!useRegExp.isSelected()) {
                exp = "\\Q"+exp+"\\E";
            }
            Pattern p = Pattern.compile(exp);
            Matcher m = p.matcher(jTextArea.getText());
            occurrences.clear();
            position.set(0);
            while (m.find()) {
                occurrences.add(new Occurrence(m.start(), m.end()));
            }
            if (occurrences.size()>0) {
                showOccurrence(occurrences.get(0), jTextArea);
            }
        });
        buttonPane.add(startSearch);

        JButton previous = new JButton();
        previous.setName("PreviousMatchButton");
        previous.setText("Prev");
        previous.addActionListener( e -> {
            if (occurrences.size()>0) {
                position.compareAndSet(0, occurrences.size());
                showOccurrence(occurrences.get(position.decrementAndGet()), jTextArea);
            }
        });
        buttonPane.add(previous);

        JButton next = new JButton();
        next.setName("NextMatchButton");
        next.setText("Next");
        next.addActionListener( e -> {
            if (occurrences.size()>0) {
                position.compareAndSet(occurrences.size()-1, -1);
                showOccurrence(occurrences.get(position.incrementAndGet()), jTextArea);
            }
        });
        buttonPane.add(next);

        buttonPane.add(useRegExp);

        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);

        JMenu mFile = new JMenu();
        mFile.setName("MenuFile");
        mFile.setText("File");
        menuBar.add(mFile);

        JMenuItem mLoad = new JMenuItem();
        mLoad.setName("MenuOpen");
        mLoad.setText("Load");
        mLoad.addActionListener(load.getActionListeners()[0]);
        mFile.add(mLoad);

        JMenuItem mSave = new JMenuItem();
        mSave.setName("MenuSave");
        mSave.setText("Save");
        mSave.addActionListener(save.getActionListeners()[0]);
        mFile.add(mSave);

        mFile.addSeparator();

        JMenuItem mExit = new JMenuItem();
        mExit.setName("MenuExit");
        mExit.setText("Exit");
        mExit.addActionListener(e -> System.exit(0));
        mFile.add(mExit);

        JMenu mSearch = new JMenu();
        mSearch.setName("MenuSearch");
        mSearch.setText("Search");
        menuBar.add(mSearch);

        JMenuItem mStartSearch = new JMenuItem();
        mStartSearch.setName("MenuStartSearch");
        mStartSearch.setText("Start Search");
        mStartSearch.addActionListener(startSearch.getActionListeners()[0]);
        mSearch.add(mStartSearch);

        JMenuItem mPrev = new JMenuItem();
        mPrev.setName("MenuPreviousMatch");
        mPrev.setText("Previous");
        mPrev.addActionListener(previous.getActionListeners()[0]);
        mSearch.add(mPrev);

        JMenuItem mNext = new JMenuItem();
        mNext.setName("MenuNextMatch");
        mNext.setText("Next");
        mNext.addActionListener(next.getActionListeners()[0]);
        mSearch.add(mNext);

        JMenuItem mUserRegExp = new JMenuItem();
        mUserRegExp.setName("MenuUseRegExp");
        mUserRegExp.setText("Use RegExp");
        mUserRegExp.addActionListener(
                e -> {useRegExp.setSelected(!useRegExp.isSelected());}
        );
        mSearch.add(mUserRegExp);
        setVisible(true);
    }

    private void showOccurrence(Occurrence occurrence, JTextArea jTextArea) {
        jTextArea.setCaretPosition(occurrence.end);
        jTextArea.select(occurrence.start, occurrence.end);
        jTextArea.grabFocus();
    }

    public static void main(String[] args) {
        new TextEditor();
    }

    private static class Occurrence {
        public int start;
        public int end;

        public Occurrence(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
