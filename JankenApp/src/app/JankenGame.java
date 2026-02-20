package app;

import mode.BasicModePanel;
import mode.PointModePanel;
import mode.ReverseModePanel;
import search.PlayerSearchFrame;
import stats.GraphFrame;
import ui.HandIcons;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class JankenGame extends JFrame {

    private final CardLayout layout = new CardLayout();
    private final JPanel mainPanel = new JPanel(layout);
    private final Random random = new Random();

    private final ImageIcon[] handIcons = HandIcons.defaultIcons();

    public JankenGame() {
        setTitle("じゃんけんゲーム");
        setSize(600, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        showMenu();

        add(mainPanel);
        layout.show(mainPanel, "MENU");
    }

    private void showMenu() {
        JPanel p = new JPanel(new GridLayout(6, 1, 15, 15));
        p.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        JButton b1 = bigButton("基本モード");
        JButton b2 = bigButton("ポイント制モード");
        JButton b3 = bigButton("後出しモード");
        JButton b4 = bigButton("統計表示");
        JButton b5 = bigButton("履歴検索");

        b1.addActionListener(e -> openMode("BASIC"));
        b2.addActionListener(e -> openMode("POINT"));
        b3.addActionListener(e -> openMode("REVERSE"));

        b4.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "名前入力");
            if (name != null && !name.isBlank()) new GraphFrame(name).setVisible(true);
        });

        b5.addActionListener(e -> new PlayerSearchFrame().setVisible(true));

        p.add(b1);
        p.add(b2);
        p.add(b3);
        p.add(b4);
        p.add(b5);

        mainPanel.add(p, "MENU");
    }

    private void openMode(String mode) {
        String name = JOptionPane.showInputDialog(this, "名前入力");
        if (name == null || name.isBlank()) return;

        ScoreManager.setCurrentPlayer(name);

        Runnable backToMenu = () -> layout.show(mainPanel, "MENU");

        JPanel panel = switch (mode) {
            case "BASIC" -> new BasicModePanel(backToMenu, handIcons, random).build();
            case "POINT" -> new PointModePanel(backToMenu, handIcons, random).build();
            case "REVERSE" -> new ReverseModePanel(backToMenu, handIcons, random).build();
            default -> null;
        };

        if (panel == null) return;

        mainPanel.add(panel, mode);
        layout.show(mainPanel, mode);
    }

    private JButton bigButton(String t) {
        JButton b = new JButton(t);
        b.setFont(new Font("Meiryo", Font.BOLD, 18));
        return b;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JankenGame().setVisible(true));
    }
}