package mode;

import app.ScoreManager;
import ui.CpuAnimator;
import ui.HandButtonsPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class BasicModePanel implements ModePanel {

    private static final int TOTAL_ROUNDS = 10;

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
        JPanel root = new JPanel(new BorderLayout(10, 10));

        JButton back = new JButton("戻る");
        back.addActionListener(e -> onBackToMenu.run());
        root.add(back, BorderLayout.NORTH);

        JLabel title = label("残り" + TOTAL_ROUNDS + "回");
        JLabel rateLabel = label("勝率: 0.0%");
        JLabel resultLabel = label("結果: -");
        JLabel cpuImage = new JLabel(handIcons[0], SwingConstants.CENTER);

        JPanel center = new JPanel(new GridLayout(4, 1, 10, 10));
        center.add(title);
        center.add(rateLabel);
        center.add(resultLabel);
        center.add(cpuImage);
        root.add(center, BorderLayout.CENTER);

        // state: [remain, win, played]
        int[] state = new int[]{TOTAL_ROUNDS, 0, 0};

        CpuAnimator animator = new CpuAnimator(cpuImage, handIcons, random, 80);
        animator.start();

        HandButtonsPanel buttons = new HandButtonsPanel(handIcons, playerHand -> {
            if (state[0] <= 0) return;

            // CPUアニメ停止＆現在手を確定
            animator.stop();
            int cpuHand = animator.getCurrentHand();
            cpuImage.setIcon(handIcons[cpuHand]);

            // 勝敗判定
            int result = judge(playerHand, cpuHand); // 1=勝ち 0=あいこ -1=負け
            if (result == 1) state[1]++;

            // 回数更新（←これがカウントダウン本体）
            state[2]++;   // played++
            state[0]--;   // remain--

            // 表示更新（EDTで確実に反映）
            SwingUtilities.invokeLater(() -> {
                title.setText("残り" + state[0] + "回");

                double rate = (state[2] == 0) ? 0.0 : (state[1] * 100.0 / state[2]);
                rateLabel.setText(String.format("勝率: %.1f%%", rate));

                resultLabel.setText("結果: " + HandButtonsPanel.handName(playerHand)
                        + " / CPU " + HandButtonsPanel.handName(cpuHand));

                title.revalidate();
                title.repaint();
                root.revalidate();
                root.repaint();
            });

            // 終了 or 次ラウンド
            if (state[0] == 0) {
                finishGame(state[1]);
            } else {
                Timer t = new Timer(800, ev -> {
                    animator.start();
                    ((Timer) ev.getSource()).stop();
                });
                t.setRepeats(false);
                t.start();
            }
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
        return -1;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Meiryo", Font.PLAIN, 16));
        return l;
    }

    private void finishGame(int win) {
        String player = ScoreManager.getCurrentPlayer();
        if (player != null && !player.isBlank()) {
            ScoreManager.commitGame(player, win);
        }
        JOptionPane.showMessageDialog(null, ScoreManager.getRanking());
        onBackToMenu.run();
    }
}