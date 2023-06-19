package raf.agents;


import raf.principal.Agent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** MUY IMPORTANTE: COMENTAR LA CLASE ENTERA ANTES DE EJECUTAR. CTRL+A Y CTRL+/ (DEL NUMPAD)
 */
//(Doesn't work)
public class TicTacToe extends Agent {
    private TicTacToeUI frame;
    private final JButton[][] buttons = new JButton[3][3];
    private boolean player = false;
    private boolean gameOver = false;
    private boolean actionPerformed;

    public TicTacToe(String name) {
        super("TicTacToe_" + name);
    }


    public void run() {
        frame = new TicTacToeUI();

    }

    public class TicTacToeUI extends JFrame implements ActionListener {

        public TicTacToeUI() {
            setTitle("Ta te ti");
            setSize(300, 300);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(3, 3));

            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    JButton button = new JButton();
                    button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 50));
                    button.addActionListener(this);
                    buttons[row][col] = button;
                    panel.add(button);
                }
            }

            add(panel);
            setVisible(true);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if (actionPerformed) return;
            if (gameOver) {
                fireDestroyRequest();

            }
            actionPerformed = true;

            JButton button = (JButton) e.getSource();
            if (!button.getText().isEmpty()) {
                return;
            }
            if (player) {
                button.setText("X");
            } else {
                button.setText("O");
            }

            if (checkForWin()) {
                String winner = player ? "X" : "O";
                JOptionPane.showMessageDialog(this, "Player " + winner + " wins!");
                gameOver = true;
            } else if (checkForDraw()) {
                JOptionPane.showMessageDialog(this, "It's a draw!");
                gameOver = true;
            } else {
                player = !player;
            }

            Timer timer = new Timer(2000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    TicTacToeUI.this.dispose();
                }
            });
            timer.setRepeats(false);
            timer.start();

        }
   }

    private boolean checkForWin() {
        for (int row = 0; row < 3; row++) {
            if (!buttons[row][0].getText().isEmpty() && buttons[row][0].getText().equals(buttons[row][1].getText())
                    && buttons[row][0].getText().equals(buttons[row][2].getText())) {
                return true;
            }
        }
        for (int col = 0; col < 3; col++) {
            if (!buttons[0][col].getText().isEmpty() && buttons[0][col].getText().equals(buttons[1][col].getText())
                    && buttons[0][col].getText().equals(buttons[2][col].getText())) {
                return true;
            }
        }
        if (!buttons[0][0].getText().isEmpty() && buttons[0][0].getText().equals(buttons[1][1].getText())
                && buttons[0][0].getText().equals(buttons[2][2].getText())) {
            return true;
        }
        if (!buttons[0][2].getText().isEmpty() && buttons[0][2].getText().equals(buttons[1][1].getText())
                && buttons[0][2].getText().equals(buttons[2][0].getText())) {
            return true;
        }
        return false;
    }

    private boolean checkForDraw() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (buttons[row][col].getText().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
}