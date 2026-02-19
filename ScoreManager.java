import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScoreManager {

    private static final String DB_URL = "jdbc:sqlite:janken.db";
    private static String currentPlayer;

    // ドライバ読み込み＆テーブル作成
    static {
        try {
            Class.forName("org.sqlite.JDBC"); // SQLiteドライバ読み込み
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try (Connection c = DriverManager.getConnection(DB_URL);
             Statement s = c.createStatement()) {

            s.executeUpdate("""
                CREATE TABLE IF NOT EXISTS game_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    hand TEXT NOT NULL,
                    reaction_ms INTEGER,
                    points INTEGER
                )
            """);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 現在のプレイヤー
    public static void setCurrentPlayer(String name) {
        currentPlayer = name;
    }

    public static String getCurrentPlayer() {
        return currentPlayer;
    }

    // ゲーム終了時にポイント保存
    public static void commitGame(String name, int points) {
        try (Connection c = DriverManager.getConnection(DB_URL);
             PreparedStatement ps =
                     c.prepareStatement("INSERT INTO game_logs(name, hand, points) VALUES (?, ?, ?)")) {

            ps.setString(1, name);
            ps.setString(2, "-"); // ここはまとめ保存なので手は"-"
            ps.setInt(3, points);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 個別の手と反応時間を一時保存
    public static void saveHandTemp(String hand, long reaction) {
        try (Connection c = DriverManager.getConnection(DB_URL);
             PreparedStatement ps =
                     c.prepareStatement("INSERT INTO game_logs(name, hand, reaction_ms) VALUES (?, ?, ?)")) {

            ps.setString(1, currentPlayer != null ? currentPlayer : "Unknown");
            ps.setString(2, hand);
            ps.setLong(3, reaction);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 総プレイ回数
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

    // 平均反応時間
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

    // 手の使用回数まとめ
    public static String getHandSummary(String name) {
        StringBuilder sb = new StringBuilder();
        try (Connection c = DriverManager.getConnection(DB_URL);
             PreparedStatement ps =
                     c.prepareStatement("SELECT hand, COUNT(*) FROM game_logs WHERE name=? AND hand != '-' GROUP BY hand")) {

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

    // 総合ランキング（ポイント順）
    public static String getRanking() {
        StringBuilder sb = new StringBuilder("【総合ランキング】\n");
        try (Connection c = DriverManager.getConnection(DB_URL);
             Statement s = c.createStatement()) {

            ResultSet rs = s.executeQuery("SELECT name, SUM(points) AS total FROM game_logs GROUP BY name ORDER BY total DESC");

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