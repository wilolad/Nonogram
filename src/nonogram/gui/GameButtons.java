package src.nonogram.gui;

import src.nonogram.non_gui.Nonogram;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class GameButtons extends JPanel {
    Nonogram puzzle;
    private JButton helpBtn;
    private JButton quitBtn;
    private JButton clearBtn;
    private JButton undoBtn;
    private JButton saveBtn;
    private JButton loadBtn;
    private JOptionPane jOptionPane;
    JFileChooser fileChooser;
    public GameButtons(Nonogram ng) {
        this.puzzle = ng;
        jOptionPane = new JOptionPane();
        fileChooser = new JFileChooser();

        helpBtn = new JButton("Help");
        clearBtn = new JButton("Clear");
        undoBtn = new JButton("Undo");
        saveBtn = new JButton("Save");
        loadBtn = new JButton("Load");
        quitBtn = new JButton("Quit");

        quitBtn.addActionListener(e -> {
                    int option =  jOptionPane.showConfirmDialog(null, "Are you sure you want to exit the game?", "Exit ?", JOptionPane.YES_NO_OPTION);
                    if (option == 0)
                        System.exit(1);
            });

        clearBtn.addActionListener(e -> {
                    int option =  jOptionPane.showConfirmDialog(null, "Are you sure you want to reset the game?", "Reset ?", JOptionPane.YES_NO_OPTION);
                    if (option == 0)
                        puzzle.clear();
            });

        saveBtn.addActionListener(this::saveAction);
        undoBtn.addActionListener(e -> puzzle.undoRecentMove());
        loadBtn.addActionListener(this::loadGameAction);

        helpBtn.addActionListener(e -> jOptionPane.showMessageDialog(null, help(), "Help!!", JOptionPane.INFORMATION_MESSAGE));

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(200, 700));

        helpBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(Box.createVerticalStrut(5));
        add(helpBtn, BorderLayout.CENTER);

        clearBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(Box.createVerticalStrut(15));
        add(clearBtn, BorderLayout.CENTER);

        undoBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(Box.createVerticalStrut(15));
        add(undoBtn, BorderLayout.CENTER);

        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(Box.createVerticalStrut(15));
        add(saveBtn, BorderLayout.CENTER);

        loadBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(Box.createVerticalStrut(15));
        add(loadBtn, BorderLayout.CENTER);

        quitBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(Box.createVerticalStrut(15));
        add(quitBtn, BorderLayout.CENTER);
    }

    /**
     * Action performed when user clicks on the save button.
     * The state of each cell is saved as integers to a file like a grid of integers
     * @param e is from the component triggering this action
     */
    private void saveAction(ActionEvent e) {

        int status = fileChooser.showSaveDialog(null);
        if (status == 0) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath().toString();
            System.out.println(filePath);
            if (puzzle.saveToFile(filePath)) {
                jOptionPane.showMessageDialog(null, "File Saved Successfully !", "Saved!", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /**
     * Display some useful help text on the puzzle when the help button is pressed
     */
    private String help() {
        String help = "Nonogram is a puzzle where you must colour in/fill in the grid according to the patterns \n"
            + "of contiguous full cells given in the rows and columns. Full cells are shown as BLACK, unknown cells as GRAY, \n"
            + "and cells you are sure are empty as WHITE. a solved row or column is marked with a \n"
            + "PINK color but it may still be wrong because of the other columns or rows - keep trying!";
        return help;
    }

    /**
     * load the saved state of the file from the
     * @param e originates from the component firing this action
     */
    private void loadGameAction(ActionEvent e) {
        int status = fileChooser.showOpenDialog(null);
        if (status == 0) {
            File file = fileChooser.getSelectedFile();
            if (file.isFile()) {
                if (puzzle.loadFromFile(file.getAbsolutePath())) {
                    jOptionPane.showMessageDialog(null, "Game Loaded !!", "Load !", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }
}
