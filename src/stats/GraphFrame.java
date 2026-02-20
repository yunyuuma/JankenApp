package stats;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GraphFrame extends JFrame {

    public GraphFrame(String playerName) {
        setTitle("統計グラフ");
        setSize(720, 520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel title = new JLabel("【" + playerName + " のプレイ統計】", SwingConstants.CENTER);
        title.setFont(new Font("Meiryo", Font.BOLD, 20));
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        JPanel graphs = new JPanel(new GridLayout(1, 2, 10, 10));
        graphs.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel pieWithTitle = new JPanel(new BorderLayout());
        JLabel pieTitle = new JLabel("手の使用割合", SwingConstants.CENTER);
        pieTitle.setFont(new Font("Meiryo", Font.BOLD, 16));
        pieWithTitle.add(pieTitle, BorderLayout.NORTH);
        pieWithTitle.add(new PiePanel(playerName), BorderLayout.CENTER);

        JPanel lineWithTitle = new JPanel(new BorderLayout());
        JLabel lineTitle = new JLabel("反応時間の推移", SwingConstants.CENTER);
        lineTitle.setFont(new Font("Meiryo", Font.BOLD, 16));
        lineWithTitle.add(lineTitle, BorderLayout.NORTH);
        lineWithTitle.add(new LinePanel(playerName), BorderLayout.CENTER);

        graphs.add(pieWithTitle);
        graphs.add(lineWithTitle);

        add(graphs, BorderLayout.CENTER);
    }

    static class PiePanel extends JPanel {
        private int gu, choki, pa;

        PiePanel(String name) {
            setPreferredSize(new Dimension(300, 300));
            try (Connection c = DriverManager.getConnection("jdbc:sqlite:janken.db");
                 PreparedStatement ps = c.prepareStatement(
                         "SELECT hand,COUNT(*) FROM game_logs WHERE name=? AND hand IN ('グー','チョキ','パー') GROUP BY hand")) {
                ps.setString(1, name);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    switch (rs.getString(1)) {
                        case "グー" -> gu = rs.getInt(2);
                        case "チョキ" -> choki = rs.getInt(2);
                        case "パー" -> pa = rs.getInt(2);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int total = gu + choki + pa;
            if (total == 0) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int size = Math.min(getWidth(), getHeight()) - 40;
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            int start = 0;
            start = drawSlice(g2, x, y, size, start, gu, total, Color.RED);
            start = drawSlice(g2, x, y, size, start, choki, total, Color.BLUE);
            start = drawSlice(g2, x, y, size, start, pa, total, Color.GREEN);

            g2.setColor(Color.BLACK);
            g2.drawString("赤=グー 青=チョキ 緑=パー", 10, 15);
        }

        private int drawSlice(Graphics2D g2, int x, int y, int size, int start, int value, int total, Color c) {
            if (value == 0) return start;
            int angle = 360 * value / total;

            g2.setColor(c);
            g2.fillArc(x, y, size, size, start, angle);

            double rad = Math.toRadians(start + angle / 2.0);
            int tx = x + size / 2 + (int) (Math.cos(rad) * size / 3);
            int ty = y + size / 2 - (int) (Math.sin(rad) * size / 3);

            g2.setColor(Color.BLACK);
            g2.drawString(value + "回", tx - 10, ty);
            g2.drawString((value * 100 / total) + "%", tx - 10, ty + 12);

            return start + angle;
        }
    }

    static class LinePanel extends JPanel {
        private final List<Integer> data = new ArrayList<>();

        LinePanel(String name) {
            try (Connection c = DriverManager.getConnection("jdbc:sqlite:janken.db");
                 PreparedStatement ps = c.prepareStatement(
                         "SELECT reaction_ms FROM game_logs WHERE name=? AND reaction_ms IS NOT NULL ORDER BY id")) {
                ps.setString(1, name);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) data.add(rs.getInt(1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data.size() < 2) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int left = 50, bottom = getHeight() - 40;
            int width = getWidth() - 80, height = getHeight() - 80;

            int max = data.stream().max(Integer::compare).orElse(1);
            int avg = (int) data.stream().mapToInt(i -> i).average().orElse(0);

            // grid
            g2.setColor(Color.LIGHT_GRAY);
            for (int i = 0; i <= 5; i++) {
                int y = bottom - i * height / 5;
                g2.drawLine(left, y, left + width, y);
            }

            // avg line
            int avgY = bottom - avg * height / max;
            g2.setColor(Color.ORANGE);
            g2.drawLine(left, avgY, left + width, avgY);
            g2.drawString("AVG " + avg + "ms", left + width - 80, avgY - 5);

            // line
            g2.setColor(Color.BLUE);
            int prevX = left;
            int prevY = bottom - data.get(0) * height / max;
            for (int i = 1; i < data.size(); i++) {
                int x = left + i * width / (data.size() - 1);
                int y = bottom - data.get(i) * height / max;
                g2.drawLine(prevX, prevY, x, y);
                g2.fillOval(x - 3, y - 3, 6, 6);
                prevX = x;
                prevY = y;
            }
        }
    }
}