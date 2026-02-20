package search;

import app.ScoreManager;

import javax.swing.*;
import java.awt.*;

public class PlayerSearchFrame extends JFrame {

    public PlayerSearchFrame() {
        setTitle("プレイヤー検索");
        setSize(420, 320);
        setLocationRelativeTo(null);

        String name = JOptionPane.showInputDialog(this, "プレイヤー名を入力してください");

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Meiryo", Font.PLAIN, 14));

        if (name == null || name.isBlank()) {
            area.setText("名前が入力されていません。");
            add(new JScrollPane(area));
            return;
        }

        int count = ScoreManager.getPlayCount(name);
        double avg = ScoreManager.getAverageReaction(name);
        String hands = ScoreManager.getHandSummary(name);

        String msg = "【" + name + " の履歴データ】\n\n" +
                "総プレイ回数：" + count + "回\n" +
                "平均反応時間：" + String.format("%.1f", avg) + " ms\n\n" +
                "手の使用回数：\n" + hands + "\n\n" +
                "――――――――――――――\n" + ScoreManager.getRanking();

        area.setText(msg);
        add(new JScrollPane(area));
    }
}