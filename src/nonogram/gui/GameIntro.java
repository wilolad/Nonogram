package src.nonogram.gui;

import src.nonogram.non_gui.NonogramUI;

import javax.swing.*;
import java.awt.*;

public class GameIntro extends JFrame {
    JPanel panel;
    JButton start, exit;
    JLabel welcomeText;
    private static final String welcomeMsg = "<html>&emsp;<h2>Welcome to the Nonogram Game</h2><br/>" +
        "&emsp;&emsp;<h3>Select your preferred interface and click on <br/> &emsp;&emsp;the start button to proceed.</h3><br/>" +
        "</html>";

    public GameIntro() {
        panel = new JPanel();
        start = new JButton("Start Game");
        exit = new JButton("Exit");

        welcomeText = new JLabel(welcomeMsg);
        welcomeText.setForeground(new Color(9, 46, 89));

        ButtonGroup group = new ButtonGroup();

        JRadioButton gui = new JRadioButton("GUI");
        JRadioButton textUI = new JRadioButton("Text UI");
        gui.setSelected(true);

        group.add(gui);
        group.add(textUI);

        start.addActionListener(e -> {
                    if (gui.isSelected()) {
                        this.dispose();
                        displayGUI();
                    } else {
                        this.dispose();
                        displayTextUI();
                    }
            });

        exit.addActionListener(e -> System.exit(1));

        panel.add(welcomeText);
        panel.add(Box.createHorizontalStrut(70));
        panel.add(gui);
        panel.add(textUI);
        panel.add(Box.createHorizontalStrut(130));
        panel.add(Box.createRigidArea(new Dimension(400, 10)));
        panel.add(exit);
        panel.add(start);
        add(panel);

        setSize(400, 300);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
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
