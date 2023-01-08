package src.nonogram.gui;


import src.nonogram.non_gui.Cell;
import src.nonogram.non_gui.Nonogram;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * A Graphical User interface to a Nonogram puzzle.
 */

public class GUI implements Observer {
    JFrame mainFrame;
    JPanel mainPanel, gamePanel;

    /**
     * Default constructor
     */
    public GUI() {
        Scanner fs = null;
        try {
            fs = new Scanner(new File(NGFILE));
            puzzle = new Nonogram(fs);
        } catch (FileNotFoundException e) {
            System.out.println(NGFILE + " not found");
            System.exit(1);
        }
        mainFrame = new JFrame();
        mainFrame.setTitle("Nonogram Game");
        btnPanel = new GameButtons(puzzle);
        puzzle.addObserver(this);
        createUI();
    }

    /**
     * Create and add all UI components to this JFrame and
     * displays it to the end user.
     */
    public void createUI() {
        mainPanel = new JPanel();
        gamePanel = new JPanel();
        int numCols = puzzle.getNumCols();
        int numRows = puzzle.getNumRows();

        rowNums = new int[numRows][];
        colNums = new int[numCols][];

        int gridColSize = numCols + 2;
        int gridRowSize = numRows + 2;

        mainPanel.setLayout(new BorderLayout());
        gamePanel.setLayout(new BoxLayout(gamePanel, BoxLayout.X_AXIS));

        btnCells = new JButton[numRows + 1][numCols + 1];
        cellsPanel = new CellsPanel(puzzle, btnCells, gridRowSize, gridColSize);

        rowHintPanel = new JPanel(new GridLayout(gridRowSize, 1));
        columnHintPanel = new JPanel(new GridLayout(1, gridColSize));

        for (int c = 0; c < numCols; c++) {
            colNums[c] = puzzle.getColNums(c);
            if (colNums[c].length > maxColNumsLen)
                maxColNumsLen = colNums[c].length;
        }

        for (int r = 0; r < numRows; r++) {
            rowNums[r] = puzzle.getRowNums(r);
            if (rowNums[r].length > maxRowNumsLen)
                maxRowNumsLen = rowNums[r].length;
        }


        for (int i = 0; i < maxColNumsLen; i++) {
            for (int c = 0; c < numCols; c++)
                if (i < colNums[c].length) {
                    if (i > 0) {
                        String clew = colHint.get(c) + " " + colNums[c][i];
                        colHint.remove(c);
                        colHint.add(c, clew);
                    } else {
                        colHint.add(String.valueOf(colNums[c][i]));
                    }
                }
        }

        for (int r = 0; r < numRows; r++) {
            StringBuilder clewBuilder = new StringBuilder();
            for (int i = 0; i < rowNums[r].length; i++) {
                clewBuilder.append(rowNums[r][i]);
                if (i < rowNums[r].length - 1)
                    clewBuilder.append(" ");
            }
            rowHint.add(clewBuilder.toString());
        }

        columnHintPanel.setBackground(HINT);
        columnHintPanel.add(new JLabel());

        for (String c : colHint) {
            JLabel hintLabel = new JLabel(transformStringToVertical(c));
            hintLabel.setForeground(Color.WHITE);
            hintLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            columnHintPanel.add(hintLabel);
        }

        rowHintPanel.setBackground(HINT);
        rowHintPanel.add(new JLabel()); //add empty label to the unused cell
        for (String r : rowHint) {
            JLabel lab = new JLabel(formatRowHint(r));
            lab.setForeground(Color.WHITE);
            lab.setHorizontalAlignment(SwingConstants.LEADING);
            rowHintPanel.add(lab);
        }

        mainPanel.add(columnHintPanel, BorderLayout.NORTH);
        mainPanel.add(cellsPanel, BorderLayout.CENTER);
        mainPanel.add(rowHintPanel, BorderLayout.WEST);

        mainFrame.setLayout(new BoxLayout(mainFrame.getContentPane(), BoxLayout.LINE_AXIS));

        int frameWidth = (int) mainFrame.getSize().getWidth();
        int frameHeight = (int) mainFrame.getSize().getHeight();

        mainFrame.setSize(900, 700);

        mainPanel.setPreferredSize(new Dimension((int) (frameWidth * .8), frameHeight));

        mainFrame.add(mainPanel, BoxLayout.X_AXIS);
        mainFrame.add(Box.createHorizontalStrut(5));
        mainFrame.add(btnPanel, BoxLayout.X_AXIS);

        mainFrame.setPreferredSize(new Dimension(900, 700));
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.pack();

        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    /**
     * Transform the given string into a vertical text by inserting HTML line breaks between each letter.
     *
     * @param str the string to be converted
     * @return the converted string in vertical text format
     */
    public static String transformStringToVertical(String str) {
        String htmlStr = "<html>";
        String br = "<br>";
        String[] letters = str.split("");
        for (String l : letters) {
            htmlStr += br + l;
        }
        htmlStr += "</html>";
        return htmlStr;
    }

    /**
     * Format a string by adding double spaces before each letter and a space afterwards
     *
     * @param str the string to be formatted.
     * @return the formatted string.
     */
    public static String formatRowHint(String str) {
        String formattedStr = "";
        String space = " ";
        String[] lettersArray = str.split("");
        for (String letter : lettersArray) {
            formattedStr += space + space + letter + space;
        }
        return formattedStr;
    }

    /**
     * This method updates the visual representation of a cell in the game when it has changed state.
     * If arg (the cell) is null, the game is reset. Otherwise, the row, column, and state of the cell are
     * retrieved and used to update the background and foreground colors of the corresponding button
     * on the game board.
     *
     * @param o   the Observable object that has changed state
     * @param arg the Object representing the cell that has changed state
     */
    @Override
    public void update(Observable o, Object arg) {
        if (arg != null) {
            int row = ((Cell) arg).getRow();
            int col = ((Cell) arg).getCol();
            int state = ((Cell) arg).getState();

            JButton btn = btnCells[row + 1][col + 1];

            btn.setForeground(getButtonColor(state));
            btn.setBackground(getButtonColor(state));

            checkSolved(row, col);
        } else {
            resetGame();
        }

    }

    /**
     * This method checks if a row or column or the entire puzzle is solved.
     * When a row or col is solved, the row or col hint changes to green
     *
     * @param row the row we want to check for solved
     * @param col the column we want to check for solved
     */
    private void checkSolved(int row, int col) {
        if (puzzle.isRowSolved(row)) {
            rowHintPanel.getComponents()[(row + 1)].setForeground(Color.GREEN);
            this.mainFrame.repaint();
        } else {
            rowHintPanel.getComponents()[(row + 1)].setForeground(Color.WHITE);
            this.mainFrame.repaint();
        }

        if (puzzle.isColSolved(col)) {
            columnHintPanel.getComponents()[(col + 1)].setForeground(Color.GREEN);
            this.mainFrame.repaint();
        } else {
            columnHintPanel.getComponents()[(col + 1)].setForeground(Color.WHITE);
            this.mainFrame.repaint();
        }

        if (puzzle.isSolved()) {
            (new JOptionPane()).showMessageDialog(null, "Well Done!!!, You have solved the Puzzle !!.",
                    "Puzzle Solved", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Reset the game by setting the background and foreground of all cells to the GRAY color defined by UNKNOWN.
     */
    public void resetGame() {
        for (int i = 0; i < btnCells.length; i++) {
            for (int j = 0; j < btnCells[i].length; j++) {
                if (j < 1 || i < 1) {
                    btnCells[i][j].setBackground(EMPTY);
                    continue;
                }
                btnCells[i][j].setForeground(UNKNOWN);
                btnCells[i][j].setBackground(UNKNOWN);
            }
        }
    }

    /**
     * Return a color based on the state of a cell.
     *
     * @param state an integer representing the state of the cell.
     *              Possible values are 0 for empty, 1 for full, and any other value for unknown.
     * @return a Color object representing the corresponding state of the button.
     * EMPTY for color White, FULL for Black, and UNKNOWN for Gray.
     */
    public static Color getButtonColor(int state) {
        if (state == 1) {
            return FULL;
        } else if (state == 2) {
            return UNKNOWN;
        } else {
            return EMPTY;
        }
    }

    private Scanner scnr = null;
    private Nonogram puzzle = null;

    private static final String NGFILE = "nons\\10.non";
    private static final Color UNKNOWN = new Color(203, 199, 199, 235);

    private static final Color HINT = new Color(20, 43, 58, 165);
    private static final Color FULL = Color.BLACK;
    private static final Color EMPTY = Color.WHITE;
    private JPanel rowHintPanel;
    private JPanel columnHintPanel;
    private GameButtons btnPanel;
    private CellsPanel cellsPanel;
    private JButton[][] btnCells;
    List<String> colHint = new ArrayList<>();
    List<String> rowHint = new ArrayList<>();

    int[][] rowNums;
    int[][] colNums;
    int maxRowNumsLen = 0;
    int maxColNumsLen = 0;
}
