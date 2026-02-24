package mode;

import app.ScoreManager;
import ui.HandButtonsPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class ReverseModePanel implements ModePanel {

    private final Runnable onBackToMenu;
    private final ImageIcon[] handIcons;
    private final Random random;

    // CPUの手を保持
    private int currentCpuHand = 0;

    // 入力受付フラグ
    private boolean waitingInput = false;

    // 状態
    private int remain = 10;
    private int score = 0;

    // テンポ・制限時間
    private static final int CHANT_MS = 400;
    private static final int LIMIT_MS = 1000;
    private static final int NEXT_ROUND_WAIT_MS = 1000;

    public ReverseModePanel(Runnable onBackToMenu, ImageIcon[] handIcons, Random random) {
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
        JLabel scoreLabel = normal("スコア: 0");
        JLabel chantLabel = big("じゃん");

        JComboBox<String> goalBox = new JComboBox<>(new String[]{"勝つ", "負ける"});
        JLabel cpuImage = new JLabel(handIcons[0], SwingConstants.CENTER);

        JPanel center = new JPanel(new GridLayout(5, 1, 10, 10));
        center.add(title);
        center.add(goalBox);
        center.add(scoreLabel);
        center.add(chantLabel);
        center.add(cpuImage);

        root.add(center, BorderLayout.CENTER);

        HandButtonsPanel buttons = new HandButtonsPanel(handIcons, playerHand -> {

            if (!waitingInput) return;

            waitingInput = false;

            boolean success = isGoalAchieved(
                    playerHand,
                    currentCpuHand,
                    (String) goalBox.getSelectedItem()
            );

            if (success) {
                score++;
                chantLabel.setText("成功！");
            } else {
                score--;
                chantLabel.setText("失敗！");
            }

            nextRound(title, scoreLabel, chantLabel, cpuImage);
        });

        root.add(buttons, BorderLayout.SOUTH);

        // 初回開始
        startRound(title, scoreLabel, chantLabel, cpuImage);

        return root;
    }

    private void startRound(JLabel title,
                            JLabel scoreLabel,
                            JLabel chantLabel,
                            JLabel cpuImage) {

        if (remain <= 0) {
            finishGame();
            return;
        }

        title.setText("残り" + remain + "回");
        scoreLabel.setText("スコア: " + score);

        waitingInput = false;
        chantLabel.setText("じゃん");

        // じゃん
        Timer t1 = new Timer(CHANT_MS, e1 -> {
            chantLabel.setText("けん");

            // けん
            Timer t2 = new Timer(CHANT_MS, e2 -> {
                chantLabel.setText("ぽん！");

                // CPU手決定（先に出す）
                currentCpuHand = random.nextInt(3);
                cpuImage.setIcon(handIcons[currentCpuHand]);

                waitingInput = true;

                // TIME UP（制限時間）
                Timer timeout = new Timer(LIMIT_MS, e3 -> {
                    if (waitingInput) {
                        waitingInput = false;
                        chantLabel.setText("TIME UP");
                        score--;
                        nextRound(title, scoreLabel, chantLabel, cpuImage);
                    }
                    ((Timer) e3.getSource()).stop();
                });
                timeout.setRepeats(false);
                timeout.start();

                ((Timer) e2.getSource()).stop();
            });
            t2.setRepeats(false);
            t2.start();

            ((Timer) e1.getSource()).stop();
        });
        t1.setRepeats(false);
        t1.start();
    }

    private void nextRound(JLabel title,
                           JLabel scoreLabel,
                           JLabel chantLabel,
                           JLabel cpuImage) {

        remain--;

        Timer next = new Timer(NEXT_ROUND_WAIT_MS, e -> {
            ((Timer) e.getSource()).stop();
            startRound(title, scoreLabel, chantLabel, cpuImage);
        });
        next.setRepeats(false);
        next.start();
    }

    private boolean isGoalAchieved(int player, int cpu, String goal) {
        int j = judge(player, cpu); // 1=勝ち 0=あいこ -1=負け

        if ("勝つ".equals(goal)) return j == 1;
        if ("負ける".equals(goal)) return j == -1;

        return false;
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
        l.setFont(new Font("Meiryo", Font.BOLD, 26));
        return l;
    }

    private JLabel normal(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Meiryo", Font.PLAIN, 16));
        return l;
    }

    private void finishGame() {
        String player = ScoreManager.getCurrentPlayer();
        if (player != null && !player.isBlank()) {
            ScoreManager.commitGame(player, score);
        }

        JOptionPane.showMessageDialog(null,
                "最終スコア: " + score + "\n" + ScoreManager.getRanking());

        onBackToMenu.run();
    }
}