package ball;

import javax.swing.*;

/** Jeu de balle (casse-briques) en Java Swing. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Jeu de Ball - Casse-briques");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            GamePanel p = new GamePanel();
            f.add(p);
            f.pack();
            f.setResizable(false);
            f.setLocationRelativeTo(null);
            f.setVisible(true);
            p.requestFocusInWindow();
        });
    }
}
