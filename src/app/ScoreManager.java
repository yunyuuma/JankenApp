package app;

import java.sql.*;

public class ScoreManager {

    private static final String DB_URL = "jdbc:sqlite:janken.db";
    private static String currentPlayer;

    static {
        // ドライバ読み込み
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // テーブル作成
        try (Connection c = DriverManager.getConnection(DB_URL);
             Statement s = c.createStatement()) {

            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS game_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    hand TEXT NOT NULL,
                    reaction_ms INTEGER,
                    points INTEGER DEFAULT 0
                )
            """);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setCurrentPlayer(String name) {
        currentPlayer = name;
    }

    public static String getCurrentPlayer() {
        return currentPlayer;
    }

    // ゲーム終了時：ポイント保存
    public static void commitGame(String name, int points) {
        try (Connection c = DriverManager.getConnection(DB_URL);
             PreparedStatement ps =
                     c.prepareStatement("INSERT INTO game_logs(name, hand, points) VALUES (?, ?, ?)")) {

            ps.setString(1, name);
            ps.setString(2, "-"); // まとめ保存
            ps.setInt(3, points);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 手と反応時間（1回ごと）保存
    public static void saveHandTemp(String hand, long reaction) {
        String name = (currentPlayer != null && !currentPlayer.isBlank()) ? currentPlayer : "Unknown";

        try (Connection c = DriverManager.getConnection(DB_URL);
             PreparedStatement ps =
                     c.prepareStatement("INSERT INTO game_logs(name, hand, reaction_ms, points) VALUES (?, ?, ?, 0)")) {

            ps.setString(1, name);
            ps.setString(2, hand);
            ps.setLong(3, reaction);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getPlayCount(String name) {
        try (Connection c = DriverManager.getConnection(DB_URL);
             PreparedStatement ps =
                     c.prepareStatement("SELECT COUNT(*) FROM game_logs WHERE name=?")) {

            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static double getAverageReaction(String name) {
        try (Connection c = DriverManager.getConnection(DB_URL);
             PreparedStatement ps =
                     c.prepareStatement("SELECT AVG(reaction_ms) FROM game_logs WHERE name=? AND reaction_ms IS NOT NULL")) {

            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public static String getHandSummary(String name) {
        StringBuilder sb = new StringBuilder();
        try (Connection c = DriverManager.getConnection(DB_URL);
             PreparedStatement ps =
                     c.prepareStatement("SELECT hand, COUNT(*) FROM game_logs WHERE name=? AND hand IN ('グー','チョキ','パー') GROUP BY hand")) {

            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                sb.append(rs.getString(1))
                  .append("：")
                  .append(rs.getInt(2))
                  .append("回\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String getRanking() {
        StringBuilder sb = new StringBuilder("【総合ランキング】\n");
        try (Connection c = DriverManager.getConnection(DB_URL);
             Statement s = c.createStatement()) {

            ResultSet rs = s.executeQuery("""
                SELECT name, SUM(points) AS total
                FROM game_logs
                GROUP BY name
                ORDER BY total DESC
            """);

            int rank = 1;
            while (rs.next()) {
                sb.append(rank)
                  .append("位: ")
                  .append(rs.getString("name"))
                  .append(" / ")
                  .append(rs.getInt("total"))
                  .append("ポイント\n");
                rank++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}