package ui;

import app.ScoreManager;

import javax.swing.*;
import java.awt.*;

public class HandButtonsPanel extends JPanel {

    public interface HandAction { void run(int playerHand); }

    private long actionStartTime = System.currentTimeMillis();

    public HandButtonsPanel(ImageIcon[] handIcons, java.util.function.IntConsumer action) {
        super(new GridLayout(1, 3, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < 3; i++) {
            int player = i;
            JButton b = new JButton(handIcons[i]);
            b.setPreferredSize(new Dimension(handIcons[i].getIconWidth(), handIcons[i].getIconHeight()));
            b.addActionListener(e -> {
                long reaction = System.currentTimeMillis() - actionStartTime;

                action.accept(player); // 先にゲーム処理

                // 反応時間＆手を保存
                ScoreManager.saveHandTemp(handName(player), reaction);

                actionStartTime = System.currentTimeMillis();
            });
            add(b);
        }
    }

    public static String handName(int hand) {
        return switch (hand) {
            case 0 -> "グー";
            case 1 -> "チョキ";
            default -> "パー";
        };
    }
}