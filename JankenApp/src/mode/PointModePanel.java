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

        JPanel root = new JPanel(new BorderLayout(10, 10));

        JButton back = new JButton("戻る");
        back.addActionListener(e -> onBackToMenu.run());
        root.add(back, BorderLayout.NORTH);

        JLabel title = big("残り10回");
        JLabel pointLabel = normal("ポイント: 0");
        JLabel resultLabel = normal("結果: -");
        JLabel cpuImage = new JLabel(handIcons[0], SwingConstants.CENTER);

        JPanel center = new JPanel(new GridLayout(4, 1, 10, 10));
        center.add(title);
        center.add(pointLabel);
        center.add(resultLabel);
        center.add(cpuImage);

        root.add(center, BorderLayout.CENTER);

        // state: [remain, point]
        int[] state = new int[]{10, 0};

        // CPUアニメーション
        CpuAnimator animator = new CpuAnimator(cpuImage, handIcons, random, 80);
        animator.start();

        HandButtonsPanel buttons = new HandButtonsPanel(handIcons, playerHand -> {

            if (state[0] <= 0) return;

            animator.stop();

            int cpuHand = animator.getCurrentHand();
            cpuImage.setIcon(handIcons[cpuHand]);

            int result = judge(playerHand, cpuHand);

            if (result == 1) {
                state[1] += 2;        // 勝ち +2
            } else if (result == -1) {
                state[1] -= 1;        // 負け -1
            }
            // あいこは変化なし

            state[0]--;

            title.setText("残り" + state[0] + "回");
            pointLabel.setText("ポイント: " + state[1]);
            resultLabel.setText(
                    "結果: " +
                            HandButtonsPanel.handName(playerHand) +
                            " / CPU " +
                            HandButtonsPanel.handName(cpuHand)
            );

            if (state[0] == 0) {
                finishGame(state[1]);
                return;
            }

            // 次ラウンド：少し待ってアニメ再開
            new Timer(800, e -> {
                animator.start();
                ((Timer) e.getSource()).stop();
            }).start();
        });

        root.add(buttons, BorderLayout.SOUTH);
        return root;
    }

    private static int judge(int player, int cpu) {

        if (player == cpu) return 0;

        // プレイヤー勝ち条件
        if ((player == 0 && cpu == 1) ||  // グー > チョキ
            (player == 1 && cpu == 2) ||  // チョキ > パー
            (player == 2 && cpu == 0)) {  // パー > グー
            return 1;
        }

        return -1; // それ以外は負け
    }

    private JLabel big(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Meiryo", Font.BOLD, 24));
        return l;
    }

    private JLabel normal(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Meiryo", Font.PLAIN, 16));
        return l;
    }

    private void finishGame(int score) {
        String player = ScoreManager.getCurrentPlayer();
        if (player != null && !player.isBlank()) {
            ScoreManager.commitGame(player, score);
        }
        JOptionPane.showMessageDialog(null, ScoreManager.getRanking());
        onBackToMenu.run();
    }
}