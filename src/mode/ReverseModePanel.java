package mode;

import app.ScoreManager;
import ui.CpuAnimator;
import ui.HandButtonsPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class ReverseModePanel implements ModePanel {

    private final Runnable onBackToMenu;
    private final ImageIcon[] handIcons;
    private final Random random;

    public ReverseModePanel(Runnable onBackToMenu, ImageIcon[] handIcons, Random random) {
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
        JLabel success = normal("成功：0");
        JComboBox<String> goal = new JComboBox<>(new String[]{"勝つ", "負ける"});
        JLabel handInfo = normal("あなた / CPU");
        JLabel cpuImage = new JLabel(handIcons[0], SwingConstants.CENTER);

        JPanel center = new JPanel(new GridLayout(5, 1, 10, 10));
        center.add(remain);
        center.add(goal);
        center.add(success);
        center.add(handInfo);
        center.add(cpuImage);
        panel.add(center, BorderLayout.CENTER);

        int[] state = {10, 0};     // 残り, 成功
        int[] showMs = {900};      // 後出し決定までの時間（短縮）
        boolean[] busy = {false};

        CpuAnimator idleAnim = new CpuAnimator(cpuImage, handIcons, random, 80);
        idleAnim.start();

        HandButtonsPanel buttons = new HandButtonsPanel(handIcons, player -> {
            if (state[0] <= 0) return;
            if (busy[0]) return;
            busy[0] = true;

            idleAnim.stop();

            // 決定までの切り替え表示
            Timer shuffle = new Timer(50, e -> cpuImage.setIcon(handIcons[random.nextInt(3)]));
            shuffle.start();

            int delay = showMs[0];
            new Timer(delay, ev -> {
                shuffle.stop();

                String g = (String) goal.getSelectedItem(); // 勝負
                int cpu = chooseCpuForGoal(player, g);       // 必ず目標どおり

                cpuImage.setIcon(handIcons[cpu]);
                handInfo.setText("あなた：" + HandButtonsPanel.handName(player) + " / CPU：" + HandButtonsPanel.handName(cpu));

                if (isGoalAchieved(player, cpu, g)) state[1]++;

                state[0]--;
                remain.setText("残り：" + state[0] + "回");
                success.setText("成功：" + state[1]);

                // だんだん短く（下限200ms）
                showMs[0] = Math.max(200, showMs[0] - 80);

                if (state[0] == 0) {
                    finishGame(state[1]);
                } else {
                    idleAnim.start();
                    busy[0] = false;
                }

                ((Timer) ev.getSource()).stop();
            }).start();
        });

        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private int chooseCpuForGoal(int player, String goal) {
        // hand: 0=グー 1=チョキ 2=パー
        // playerが勝つ → cpuは playerに負ける手： (player+1)%3
        // playerが負ける → cpuは playerに勝つ手： (player+2)%3
        if ("勝つ".equals(goal)) return (player + 1) % 3;
        return (player + 2) % 3;
    }

    private boolean isGoalAchieved(int player, int cpu, String goal) {
        boolean playerWin  = ((cpu + 1) % 3 == player);
        boolean playerLose = ((player + 1) % 3 == cpu);
        if ("勝つ".equals(goal)) return playerWin;
        if ("負ける".equals(goal)) return playerLose;
        return false;
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