package mode;

import app.ScoreManager;
import ui.CpuAnimator;
import ui.HandButtonsPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class PointModePanel implements ModePanel {

    private final Runnable onBackToMenu;
    private final ImageIcon[] handIcons;
    private final Random random;

    public PointModePanel(Runnable onBackToMenu, ImageIcon[] handIcons, Random random) {
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

        JLabel remain = big("残り：10回");
        JLabel point = normal("ポイント：0");
        JLabel handInfo = normal("あなた / CPU");
        JLabel cpuImage = new JLabel(handIcons[0], SwingConstants.CENTER);

        JPanel center = new JPanel(new GridLayout(4, 1, 10, 10));
        center.add(remain);
        center.add(point);
        center.add(handInfo);
        center.add(cpuImage);
        panel.add(center, BorderLayout.CENTER);

        int[] state = {10, 0}; // 残り, ポイント

        CpuAnimator anim = new CpuAnimator(cpuImage, handIcons, random, 80);
        anim.start();

        HandButtonsPanel buttons = new HandButtonsPanel(handIcons, player -> {
            if (state[0] <= 0) return;

            int cpu = random.nextInt(3);
            cpuImage.setIcon(handIcons[cpu]);

            // player勝ち：+2 / 負け：-1 / あいこ：0
            if ((cpu + 1) % 3 == player) state[1] += 2;
            else if (player != cpu) state[1] -= 1;

            state[0]--;
            remain.setText("残り：" + state[0] + "回");
            point.setText("ポイント：" + state[1]);
            handInfo.setText("あなた：" + HandButtonsPanel.handName(player) + " / CPU：" + HandButtonsPanel.handName(cpu));

            if (state[0] == 0) finishGame(state[1]);
        });

        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JLabel big(String t) {
        JLabel l = new JLabel(t, SwingConstants.CENTER);
        l.setFont(new Font("Meiryo", Font.BOLD, 24));
        return l;
    }

    private JLabel normal(String t) {
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