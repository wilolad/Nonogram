package src.nonogram.gui;

import src.nonogram.non_gui.Assign;
import src.nonogram.non_gui.Nonogram;

import javax.swing.*;
import java.awt.*;

public class CellsPanel extends JPanel {
    private int numRows;
    private int numCols;
    private static final Color UNKNOWN = new Color(203, 199, 199, 235);
    private Nonogram puzzle = null;

    public CellsPanel(Nonogram ng, JButton[][] btnCells, int gridRow, int gridCol) {

        this.puzzle = ng;
        this.numRows = ng.getNumRows();
        this.numCols = ng.getNumCols();

        setLayout(new GridLayout(gridRow, gridCol));


        for (int i = 0; i < numRows + 1; i++) {
            for (int j = 0; j < numCols + 1; j++) {
                JButton cell = new JButton();

                cell.setBackground(Color.WHITE);
                cell.setForeground(Color.BLACK);

                cell.addActionListener(e -> {
                    JButton btn = (JButton) e.getSource();
                    Color bg = btn.getBackground();

                    int r = Integer.parseInt(e.getActionCommand().split(",")[0]) - 1;
                    
                    int c = Integer.parseInt(e.getActionCommand().split(",")[1]) - 1;

                    if (Color.black == bg) {
                        move(r, c, 0);
                    } else {
                        move(r, c, 1);
                    }

                });

                if (i == 0 && j > 0) {
                    cell.setText(String.valueOf(j - 1));
                    cell.setEnabled(false);
                }

                if (i == 0 && j == 0) {
                    cell.setText("");
                    cell.setEnabled(false);
                }

                if (j == 0 && i > 0) {
                    cell.setText(String.valueOf(i - 1));
                    cell.setEnabled(false);
                }

                if (j > 0 && i > 0) {
                    cell.setText(i + "," + j);
                    cell.setBackground(UNKNOWN);
                    cell.setForeground(UNKNOWN);
                }

                cell.setOpaque(true);
                cell.setSize(5, 5);
                add(cell);
                btnCells[i][j] = cell;
            }
        }
    }

    /**
     * makes a move updating the state of the cell to
     * @param row
     * @param col
     * @param state either FULL, UNKNOWN or EMPTY
     */
    private void move(int row, int col, int state) {
        Assign userMove = new Assign(row, col, state);
        puzzle.setState(userMove);
    }
}
