package src.nonogram.non_gui;


import java.io.*;
import java.util.*;

/**
 * A Nonogram puzzle.
 *
 * @author Dr Mark C. Sinclair
 * @version September 2022
 */
@SuppressWarnings("deprecation")
public class Nonogram extends Observable {

    public static Stack<Assign> historyStack = new Stack<>();

    /**
     * Constructor from a scanner (.non file format)
     * see https://github.com/mikix/nonogram-db/blob/master/FORMAT.md
     *
     * @param scnr the scanner
     */
    public Nonogram(Scanner scnr) {
        ArrayList<NGPattern> rowNGPatterns = new ArrayList<>();
        ArrayList<NGPattern> colNGPatterns = new ArrayList<>();
        boolean onRows = false;
        boolean onCols = false;
        while (scnr.hasNextLine()) {
            String line = scnr.nextLine();
            if (line.startsWith("width")) {
                String[] fields = line.split("\\W");
                try {
                    numCols = Integer.parseInt(fields[1]);
                } catch (NumberFormatException e) {
                    throw new NonogramException("non-integer width (" + fields[1] + ")");
                }
                if (numCols < MIN_SIZE)
                    throw new NonogramException("width cannot be shorter than " + MIN_SIZE);
            } else if (line.startsWith("height")) {
                String[] fields = line.split("\\W");
                try {
                    numRows = Integer.parseInt(fields[1]);
                } catch (NumberFormatException e) {
                    throw new NonogramException("non-integer height (" + fields[1] + ")");
                }
                if (numRows < MIN_SIZE)
                    throw new NonogramException("height cannot be shorter than " + MIN_SIZE);
            } else if (line.startsWith("rows")) {
                onRows = true;
                onCols = false;
            } else if (line.startsWith("columns")) {
                onCols = true;
                onRows = false;
            } else if (onRows && (rowNGPatterns.size() < numRows)) {
                String[] fields = line.split(",");
                int[] nums = new int[fields.length];
                int i = 0;
                try {
                    for (i = 0; i < fields.length; i++)
                        nums[i] = Integer.parseInt(fields[i].trim());
                } catch (NumberFormatException e) {
                    throw new NonogramException("non-integer num (" + fields[i] + ")");
                }
                if (!NGPattern.checkNums(nums))
                    throw new NonogramException("nums invalid");
                NGPattern pat = new NGPattern(nums, numCols);
                rowNGPatterns.add(pat);
            } else if (onCols && (colNGPatterns.size() < numCols)) {
                String[] fields = line.split(",");
                int[] nums = new int[fields.length];
                int i = 0;
                try {
                    for (i = 0; i < fields.length; i++)
                        nums[i] = Integer.parseInt(fields[i].trim());
                } catch (NumberFormatException e) {
                    throw new NonogramException("non-integer num (" + fields[i] + ")");
                }
                if (!NGPattern.checkNums(nums))
                    throw new NonogramException("nums invalid");
                NGPattern pat = new NGPattern(nums, numRows);
                colNGPatterns.add(pat);
            }
        }

        if (rowNGPatterns.size() != numRows)
            throw new NonogramException("incorrect number of rows (" + rowNGPatterns.size() + ")");
        if (colNGPatterns.size() != numCols)
            throw new NonogramException("incorrect number of cols (" + colNGPatterns.size() + ")");

        // create grid of cells
        cells = new Cell[numRows][numCols];
        for (int row = 0; row < numRows; row++)
            for (int col = 0; col < numCols; col++)
                cells[row][col] = new Cell(this, row, col);

        // create row constraints
        rows = new Constraint[numRows];
        Cell[] rowCells = new Cell[numCols];
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++)
                rowCells[col] = cells[row][col];
            rows[row] = new Constraint(rowNGPatterns.get(row), rowCells);
        }

        // create column constraints
        cols = new Constraint[numCols];
        Cell[] colCells = new Cell[numRows];
        for (int col = 0; col < numCols; col++) {
            for (int row = 0; row < numRows; row++)
                colCells[row] = cells[row][col];
            cols[col] = new Constraint(colNGPatterns.get(col), colCells);
        }
    }

    /**
     * This method returns the array of cells in the puzzle.
     * This is primarily used in the JUnit test
     * @return The array of cells in the puzzle.
     */
    public Cell[][] getCells() {
        return cells;
    }

    /**
     * This method sets the array of cells for the puzzle.
     * This is used in the NonogramTest
     * @param cells The array of cells to set for the puzzle.
     */
    public void setCells(Cell[][] cells){
        this.cells = cells;
    }

    /**
     * Retrieve the number of rows
     *
     * @return the number of rows
     */
    public int getNumRows() {
        return numRows;
    }

    /**
     * Retrieve the number of columns
     *
     * @return the number of columns
     */
    public int getNumCols() {
        return numCols;
    }

    /**
     * Retrieve the state of an individual cell
     *
     * @param row the cell row
     * @param col the cell column
     * @return the cell state
     */
    public int getState(int row, int col) {
        if ((row < 0) || (row >= numRows))
            throw new IllegalArgumentException("row invalid, must be 0 <= row < " + numRows);
        if ((col < 0) || (col >= numCols))
            throw new IllegalArgumentException("col invalid, must be 0 <= col < " + numCols);
        return cells[row][col].getState();
    }

    /**
     * Set the state of an individual cell, notifying observers
     *
     * @param row   the cell row
     * @param col   the cell column
     * @param state the new state
     */
    public void setState(int row, int col, int state) {
        if ((row < 0) || (row >= numRows))
            throw new IllegalArgumentException("row invalid, must be 0 <= row < " + numRows);
        if ((col < 0) || (col >= numCols))
            throw new IllegalArgumentException("col invalid, must be 0 <= col < " + numCols);
        if (!Cell.isValidState(state))
            throw new IllegalArgumentException("invalid state (" + state + ")");

        int current_state = cells[row][col].getState();
        cells[row][col].setState(state);
        trace("notifyObservers: row: " + row + "; col : " + col + "; state: " + state);
        setChanged();
        notifyObservers(cells[row][col]);

        //push to stack
        historyStack.push(new Assign(row, col, current_state));
    }

    /**
     * Set the state of an individual cell using the data in an Assign object
     *
     * @param move the Assign
     */
    public void setState(Assign move) {
        if (move == null)
            throw new IllegalArgumentException("cannot have null move");
        setState(move.getRow(), move.getCol(), move.getState());
    }

    /**
     * Resets the game to its initial state by setting all cells to UNKNOWN and clearing the savedHistory stack.
     * Also notifies observers of the change.
     */
    public void clear() {
        for (int row = 0; row < numRows; row++)
            for (int col = 0; col < numCols; col++)
                setState(row, col, UNKNOWN);

        setChanged();
        notifyObservers();
        historyStack.clear();
    }

    /**
     * Retrieve the pattern of contiguous full cells for a given row as an integer array
     *
     * @param row the desired row
     * @return the pattern of contiguous full cells in the row constraint
     */
    public int[] getRowNums(int row) {
        if ((row < 0) || (row >= numRows))
            throw new IllegalArgumentException("row invalid, must be 0 <= row < " + numRows);
        return rows[row].getNums();
    }

    /**
     * Retrieve the pattern of contiguous full cells for a given column as an integer array
     *
     * @param col the desired column
     * @return the pattern of contiguous full cells in the column constraint
     */
    public int[] getColNums(int col) {
        if ((col < 0) || (col >= numCols))
            throw new IllegalArgumentException("col invalid, must be 0 <= col < " + numCols);
        return cols[col].getNums();
    }

    /**
     * Retrieve the cell states for a given row as a sequence string
     *
     * @param row the desired row
     * @return the row cell states
     */
    public String getRowSequence(int row) {
        if ((row < 0) || (row >= numRows))
            throw new IllegalArgumentException("row invalid, must be 0 <= row < " + numRows);
        return rows[row].getSequence();
    }

    /**
     * Retrieve the cell states for a given column as a sequence string
     *
     * @param col the desired column
     * @return the column cell states
     */
    public String getColSequence(int col) {
        if ((col < 0) || (col >= numCols))
            throw new IllegalArgumentException("col invalid, must be 0 <= col < " + numCols);
        return cols[col].getSequence();
    }

    /**
     * Set the cell states of an entire nonogram from a single cell state string (e.g. the goal in a .non file)
     *
     * @param s the goal string
     */
    public void setStatesByString(String s) {
        if (s == null)
            throw new IllegalArgumentException("s cannot be null");
        if (s.isEmpty())
            throw new IllegalArgumentException("s cannot be empty");
        if (s.length() != numRows * numCols)
            throw new IllegalArgumentException("s must be " + numRows * numCols + " chars long (" + s.length() + ")");
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                int idx = row * numCols + col;
                int state = Nonogram.UNKNOWN;
                try {
                    state = Integer.parseInt(s.substring(idx, idx + 1));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("s contains non number (" + s.charAt(idx) + ") in s[" + idx + "]");
                }
                if (!Cell.isValidState(state))
                    throw new IllegalArgumentException("invalid state (" + state + ") in s[" + idx + "]");
                cells[row][col].setState(state);
            }
        }
    }

    /**
     * Is a given row of cells valid against its constraint?
     *
     * @param row the desired row
     * @return true if the row is valid, otherwise false
     */
    public boolean isRowValid(int row) {
        if ((row < 0) || (row >= numRows))
            throw new IllegalArgumentException("row invalid, must be 0 <= row < " + numRows);
        return rows[row].isValid();
    }

    /**
     * Is a given column of cells valid against its constraint?
     *
     * @param col the desired column
     * @return true if the column is valid, otherwise false
     */
    public boolean isColValid(int col) {
        if ((col < 0) || (col >= numCols))
            throw new IllegalArgumentException("col invalid, must be 0 <= col < " + numCols);
        return cols[col].isValid();
    }

    /**
     * Is a given row of cells solved? (Note that a row may be solved, but still incorrect depending on other columns.)
     *
     * @param row the desired row
     * @return true if the row is solved, otherwise false
     */
    public boolean isRowSolved(int row) {
        if ((row < 0) || (row >= numRows))
            throw new IllegalArgumentException("row invalid, must be 0 <= row < " + numRows);
        return rows[row].isSolved();
    }

    /**
     * Is a given column of cells solved? (Note that a column may be solved, but still incorrect depending on other rows.)
     *
     * @param col the desired column
     * @return true if the column is solved, otherwise false
     */
    public boolean isColSolved(int col) {
        if ((col < 0) || (col >= numCols))
            throw new IllegalArgumentException("col invalid, must be 0 <= col < " + numCols);
        return cols[col].isSolved();
    }

    /**
     * Are all rows and columns, and therefore the whole puzzle, solved?
     *
     * @return true if all rows and coplumns are solved, otherwise false
     */
    public boolean isSolved() {
        for (int row = 0; row < numRows; row++)
            if (!rows[row].isSolved())
                return false;
        for (int col = 0; col < numCols; col++)
            if (!cols[col].isSolved())
                return false;
        return true;
    }

    /**
     * String representation of the puzzle in .non file form
     *
     * @return the string representation
     */
    public String toStringAsNonFile() {
        StringBuffer sb = new StringBuffer();
        sb.append("width " + numCols + "\n");
        sb.append("height " + numRows + "\n");
        sb.append("\n");
        sb.append("rows\n");
        for (int row = 0; row < numRows; row++)
            sb.append(rows[row].getNumsForNon() + "\n");
        sb.append("\n");
        sb.append("columns\n");
        for (int col = 0; col < numRows; col++)
            sb.append(cols[col].getNumsForNon() + "\n");
        sb.append("\n");
        return sb.toString();
    }

    /**
     * A trace method for debugging (active when traceOn is true)
     *
     * @param s the string to output
     */
    public static void trace(String s) {
        if (traceOn)
            System.out.println("trace: " + s);
    }

    /**
     * Adds an observer to the list of observers
     * @param o  an observer to be added. In this case the GUI
     */
    @Override
    public synchronized void addObserver(Observer o) {
        super.addObserver(o);
    }

    /**
     * Save the state of the cells to a file to the file path.
     * The state of the cells are saved as a comma separated integers representing a row or column states
     * @param filePath the file path of the saved game state
     * @return true if the game was successfully saved and false otherwise
     */
    public boolean saveToFile(String filePath) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < cells.length; i++)//for each row
        {
            for (int j = 0; j < cells[i].length; j++)//for each column
            {
                builder.append(cells[i][j] + "");//append to the output string
                if (j < cells.length - 1)//if this is not the last row element
                    builder.append(",");//then add comma (if you don't like commas you can use spaces)
            }
            builder.append("\n");//append new line at the end of the row
        }
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(filePath));
            writer.write(builder.toString());//save the string representation of the cells
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    /**
     * Load a game from the specified file path.
     * @param filePath the file path of the saved game file
     * @return true if the game was successfully loaded and false if not
     */
    public boolean loadFromFile(String filePath) {

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            int row = 0;
            String line = "";
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split(",");
                int col = 0;
                for (String c : cols) {
                    int savedState = Integer.parseInt(c);
                    setState(row, col, savedState);
                    col++;
                }
                row++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    /**
     * Undo the recent move made.
     * Removes the last move made if the historyStack is not empty and updates the relevant cell with its previous state.
     * This method also notifies registered observers of the changes in the state of the cells after the undo is made.
     */
    public void undoRecentMove() {
        if (!historyStack.isEmpty()) {

            Assign prevAssign = historyStack.pop();
            Cell cell = cells[prevAssign.getRow()][prevAssign.getCol()];
            cell.setState(prevAssign.getState());

            setChanged();
            notifyObservers(cell);
        }
    }
    public static final int MIN_SIZE = 5;
    public static final int EMPTY = 0;
    public static final int FULL = 1;
    public static final int UNKNOWN = 2;
    private Cell[][] cells = null;
    private Constraint[] rows = null;
    private Constraint[] cols = null;
    private int numRows = -1;
    private int numCols = -1;
    private static boolean traceOn = false; // for debugging

}
