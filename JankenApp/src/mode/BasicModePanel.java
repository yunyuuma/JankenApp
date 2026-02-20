package mode;

import app.ScoreManager;
import ui.CpuAnimator;
import ui.HandButtonsPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class BasicModePanel implements ModePanel {

    private final Runnable onBackToMenu;
    private final ImageIcon[] handIcons;
    private final Random random;

    public BasicModePanel(Runnable onBackToMenu, ImageIcon[] handIcons, Random random) {
        this.onBackToMenu = onBackToMenu;
        this.handIcons = handIcons;
        this.random = random;
    }

    @Override
    public JPanel build() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        JButton back = new JButton("←戻る");
        back.addActionListener(e -> onBackToMenu.run());
        panel.add(back, BorderLayout.NORTH);

        JLabel cpuImage = new JLabel(handIcons[0], SwingConstants.CENTER);
        JLabel sub = label("勝率：0%");
        JLabel remain = label("残り：10回");

        JPanel center = new JPanel(new GridLayout(3, 1, 10, 10));
        center.add(cpuImage);
        center.add(sub);
        center.add(remain);
        panel.add(center, BorderLayout.CENTER);

        int[] state = {10, 0, 0}; // 残り, 勝ち, 試合数

        CpuAnimator anim = new CpuAnimator(cpuImage, handIcons, random, 80);
        anim.start();

        HandButtonsPanel buttons = new HandButtonsPanel(handIcons, player -> {
            if (state[0] <= 0) return;

            anim.stop();
            int cpu = random.nextInt(3);

            // player勝ち判定
            if ((cpu + 1) % 3 == player) state[1]++;

            state[2]++;
            state[0]--;

            double rate = state[1] * 100.0 / state[2];

            cpuImage.setIcon(handIcons[cpu]);
            sub.setText(String.format("勝率：%.1f%%", rate));
            remain.setText("残り：" + state[0] + "回");

            if (state[0] == 0) finishGame(state[1]);
            else new Timer(800, ev -> { anim.start(); ((Timer) ev.getSource()).stop(); }).start();
        });

        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JLabel label(String t) {
        JLabel l = new JLabel(t, SwingConstants.CENTER);
        l.setFont(new Font("Meiryo", Font.PLAIN, 16));
        return l;
    }

    private void finishGame(int score) {
        String name = ScoreManager.getCurrentPlayer();
        if (name != null && !name.isBlank()) ScoreManager.commitGame(name, score);
        JOptionPane.showMessageDialog(null, ScoreManager.getRanking());
        onBackToMenu.run();
    }
}