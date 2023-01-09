package src.nonogram.non_gui;

import src.nonogram.gui.GameIntro;
import javax.swing.*;
public class Main {
    /**
     * The main method of the application. It displays a dialog box with options for the user to choose their preferred
     * interface for the application (either GUI or text UI). The selected interface is then displayed.
     *
     * @param args Command line arguments passed to the application.
     */
    public static void main(String[] args) {
        new GameIntro();
    }

}