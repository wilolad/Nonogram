package src.nonogram.gui;

import src.nonogram.non_gui.NonogramUI;

import javax.swing.*;
import java.awt.*;

public class GameIntro{
    
    private static final String welcomeMsg = "<html>&emsp;<h2>Welcome to the Nonogram Game</h2><br/>" +
        "&emsp;&emsp;<h3>Select your preferred interface and click on <br/> &emsp;&emsp;the start button to proceed.</h3><br/>" +
        "</html>";

    public GameIntro() {
        String[] uiOptions = {"Text UI", "Graphical User Interface"};
        Object option = (JOptionPane.showInputDialog(null, welcomeMsg,
                "Selection", JOptionPane.DEFAULT_OPTION, null, uiOptions, "0"));

        if (option != null) {
            String selection = option.toString();
            if (selection.equalsIgnoreCase("text ui")) {
                displayTextUI();
            } else {
                displayGUI();
            }
        } else {
            System.exit(1);
        }


    }

    public static void main(String[] args) {
        new GameIntro();
    }

    /**
     * Display the main GUI for the application.
     * It creates a new instance of the GUI class and calls
     * its displayUserInterface method to display the GUI.
     */
    public static void displayGUI() {
        new GUI();
    }

    /**
     * This method displays the text-based UI for the application.
     * It creates a new instance of the NonogramUI class and calls
     * its menu method to display the UI.
     */
    public static void displayTextUI() {
        NonogramUI ui = new NonogramUI();
        ui.menu();
    }
}
