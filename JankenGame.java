import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JankenGame extends JFrame {

    private final CardLayout layout = new CardLayout();
    private final JPanel mainPanel = new JPanel(layout);
    private final Random random = new Random();
    private long actionStartTime;

    // 手画像
    private final ImageIcon[] handIcons = {
            loadIcon("gu.png"),
            loadIcon("choki.png"),
            loadIcon("pa.png")
    };

    private ImageIcon loadIcon(String name) {
        ImageIcon icon = new ImageIcon(name);
        if (icon.getIconWidth() <= 0) {
            BufferedImage img = new BufferedImage(120, 120, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, 120, 120);
            g.setColor(Color.BLACK);
            g.drawString("NO IMAGE", 20, 60);
            g.dispose();
            return new ImageIcon(img);
        }
        return icon;
    }

    public JankenGame() {
        setTitle("じゃんけんゲーム");
        setSize(600, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        showMenu();
        add(mainPanel);
        layout.show(mainPanel, "MENU");
    }

    // MENU
    private void showMenu() {
        JPanel p = new JPanel(new GridLayout(6, 1, 15, 15));
        p.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        JButton b1 = bigButton("基本モード");
        JButton b2 = bigButton("ポイント制モード");
        JButton b3 = bigButton("後出しモード");
        JButton b4 = bigButton("統計表示");
        JButton b5 = bigButton("履歴検索");

        b1.addActionListener(e -> openMode("BASIC"));
        b2.addActionListener(e -> openMode("POINT"));
        b3.addActionListener(e -> openMode("REVERSE"));
        b4.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "名前入力");
            if (name != null && !name.isBlank()) new GraphFrame(name).setVisible(true);
        });
        b5.addActionListener(e -> new PlayerSearchFrame().setVisible(true));

        p.add(b1);
        p.add(b2);
        p.add(b3);
        p.add(b4);
        p.add(b5);

        mainPanel.add(p, "MENU");
    }

    private void openMode(String mode) {
        String name = JOptionPane.showInputDialog(this, "名前入力");
        if (name == null || name.isBlank()) return;

        ScoreManager.setCurrentPlayer(name);

        JPanel panel = switch (mode) {
            case "BASIC" -> createBasic();
            case "POINT" -> createPoint();
            case "REVERSE" -> createReverse();
            default -> null;
        };

        mainPanel.add(panel, mode);
        layout.show(mainPanel, mode);
    }

    // 基本モード
    private JPanel createBasic() {
        JPanel panel = baseGamePanel();

        JLabel cpuImage = new JLabel(handIcons[0], SwingConstants.CENTER);
        JLabel sub = normalLabel("勝率：0%");
        JLabel remain = normalLabel("残り：10回");

        JPanel center = new JPanel(new GridLayout(3, 1, 10, 10));
        center.add(cpuImage);
        center.add(sub);
        center.add(remain);

        int[] state = {10, 0, 0};

        // CPUアニメーション
        Timer animation = new Timer(80, e -> cpuImage.setIcon(handIcons[random.nextInt(3)]));
        animation.start();

        JPanel btnPanel = handButtons(player -> {
            if (state[0] <= 0) return;
            animation.stop();

            int cpu = random.nextInt(3);

            if ((player + 1) % 3 == cpu) state[1]++;
            state[2]++;
            state[0]--;

            double rate = state[1] * 100.0 / state[2];

            cpuImage.setIcon(handIcons[cpu]);
            sub.setText(String.format("勝率：%.1f%%", rate));
            remain.setText("残り：" + state[0] + "回");

            if (state[0] == 0) finishGame(state[1]);
            else new Timer(800, ev -> { animation.start(); ((Timer) ev.getSource()).stop(); }).start();
        });

        panel.add(center, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    // ポイント制
    private JPanel createPoint() {
        JPanel panel = baseGamePanel();

        JLabel remain = bigLabel("残り：10回");
        JLabel point = normalLabel("ポイント：0");
        JLabel handInfo = normalLabel("あなた / CPU");
        JLabel cpuImage = new JLabel(handIcons[0], SwingConstants.CENTER);

        int[] state = {10, 0};

        JPanel center = new JPanel(new GridLayout(4, 1, 10, 10));
        center.add(remain);
        center.add(point);
        center.add(handInfo);
        center.add(cpuImage);

        // CPUアニメーション
        Timer animation = new Timer(80, e -> cpuImage.setIcon(handIcons[random.nextInt(3)]));
        animation.start();

        JPanel btnPanel = handButtons(player -> {
            if (state[0] <= 0) return;

            int cpu = random.nextInt(3);
            cpuImage.setIcon(handIcons[cpu]);

            if ((player + 1) % 3 == cpu) state[1] += 2;
            else if (player != cpu) state[1] -= 1;

            state[0]--;
            remain.setText("残り：" + state[0] + "回");
            point.setText("ポイント：" + state[1]);
            handInfo.setText("あなた：" + playerName(player) + " / CPU：" + playerName(cpu));

            if (state[0] == 0) finishGame(state[1]);
        });

        panel.add(center, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    // 後出し
    private JPanel createReverse() {
        JPanel panel = baseGamePanel();

        JLabel remain = bigLabel("残り：10回");
        JLabel success = normalLabel("成功：0");
        JComboBox<String> goal = new JComboBox<>(new String[]{"勝つ", "負ける", "あいこ"});

        int[] state = {10, 0};

        JPanel center = new JPanel(new GridLayout(3, 1, 10, 10));
        center.add(remain);
        center.add(goal);
        center.add(success);

        JPanel btnPanel = handButtons(player -> {
            if (state[0] <= 0) return;
            state[1]++;
            state[0]--;
            remain.setText("残り：" + state[0] + "回");
            success.setText("成功：" + state[1]);
            if (state[0] == 0) finishGame(state[1]);
        });

        panel.add(center, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    // 終了
    private void finishGame(int score) {
        String name = ScoreManager.getCurrentPlayer();
        if (name != null && !name.isBlank()) ScoreManager.commitGame(name, score);
        JOptionPane.showMessageDialog(this, ScoreManager.getRanking());
        layout.show(mainPanel, "MENU");
    }

    // 共通UI
    private JPanel baseGamePanel() {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        JButton back = new JButton("←戻る");
        back.addActionListener(e -> layout.show(mainPanel, "MENU"));
        p.add(back, BorderLayout.NORTH);
        return p;
    }

    private JPanel handButtons(HandAction action) {
        JPanel p = new JPanel(new GridLayout(1, 3, 10, 10));
        actionStartTime = System.currentTimeMillis();
        for (int i = 0; i < 3; i++) {
            int player = i;
            JButton b = new JButton(handIcons[i]);
            b.setPreferredSize(new Dimension(handIcons[i].getIconWidth(), handIcons[i].getIconHeight()));
            b.addActionListener(e -> {
                long reaction = System.currentTimeMillis() - actionStartTime;
                action.run(player);
                ScoreManager.saveHandTemp(playerName(player), reaction);
                actionStartTime = System.currentTimeMillis();
            });
            p.add(b);
        }
        return p;
    }

    private String playerName(int hand) {
        return switch (hand) {
            case 0 -> "グー";
            case 1 -> "チョキ";
            default -> "パー";
        };
    }

    private JButton bigButton(String t) {
        JButton b = new JButton(t);
        b.setFont(new Font("Meiryo", Font.BOLD, 18));
        return b;
    }

    private JLabel bigLabel(String t) {
        JLabel l = new JLabel(t, SwingConstants.CENTER);
        l.setFont(new Font("Meiryo", Font.BOLD, 24));
        return l;
    }

    private JLabel normalLabel(String t) {
        JLabel l = new JLabel(t, SwingConstants.CENTER);
        l.setFont(new Font("Meiryo", Font.PLAIN, 16));
        return l;
    }

    interface HandAction { void run(int player); }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JankenGame().setVisible(true));
    }

    // グラフフレーム
class GraphFrame extends JFrame {
    GraphFrame(String playerName) {
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

        // Pieグラフ
        JPanel pieWithTitle = new JPanel(new BorderLayout());
        JLabel pieTitle = new JLabel("手の使用割合", SwingConstants.CENTER);
        pieTitle.setFont(new Font("Meiryo", Font.BOLD, 16));
        pieWithTitle.add(pieTitle, BorderLayout.NORTH);
        pieWithTitle.add(new PiePanel(playerName), BorderLayout.CENTER);

        // Lineグラフ
        JPanel lineWithTitle = new JPanel(new BorderLayout());
        JLabel lineTitle = new JLabel("反応時間の推移", SwingConstants.CENTER);
        lineTitle.setFont(new Font("Meiryo", Font.BOLD, 16));
        lineWithTitle.add(lineTitle, BorderLayout.NORTH);
        lineWithTitle.add(new LinePanel(playerName), BorderLayout.CENTER);

        graphs.add(pieWithTitle);
        graphs.add(lineWithTitle);

        add(graphs, BorderLayout.CENTER);
    }
}

    class PiePanel extends JPanel {
        private int gu, choki, pa;
        PiePanel(String name) {
            setPreferredSize(new Dimension(300,300));
            try(Connection c=DriverManager.getConnection("jdbc:sqlite:janken.db");
                PreparedStatement ps=c.prepareStatement(
                        "SELECT hand,COUNT(*) FROM game_logs WHERE name=? AND hand IN ('グー','チョキ','パー') GROUP BY hand")) {
                ps.setString(1,name);
                ResultSet rs = ps.executeQuery();
                while(rs.next()){
                    switch(rs.getString(1)){
                        case "グー" -> gu=rs.getInt(2);
                        case "チョキ" -> choki=rs.getInt(2);
                        case "パー" -> pa=rs.getInt(2);
                    }
                }
            }catch(Exception e){ e.printStackTrace(); }
        }
        @Override
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            int total = gu+choki+pa;
            if(total==0) return;
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

            int size=Math.min(getWidth(),getHeight())-40;
            int x=(getWidth()-size)/2;
            int y=(getHeight()-size)/2;
            int start=0;
            start=drawSlice(g2,x,y,size,start,gu,total,Color.RED);
            start=drawSlice(g2,x,y,size,start,choki,total,Color.BLUE);
            start=drawSlice(g2,x,y,size,start,pa,total,Color.GREEN);
            g2.setColor(Color.BLACK);
            g2.drawString("赤=グー 青=チョキ 緑=パー",10,15);
        }
        private int drawSlice(Graphics2D g2,int x,int y,int size,int start,int value,int total,Color c){
            if(value==0) return start;
            int angle=360*value/total;
            g2.setColor(c);
            g2.fillArc(x,y,size,size,start,angle);
            double rad=Math.toRadians(start+angle/2.0);
            int tx=x+size/2+(int)(Math.cos(rad)*size/3);
            int ty=y+size/2-(int)(Math.sin(rad)*size/3);
            g2.setColor(Color.BLACK);
            g2.drawString(value+"回",tx-10,ty);
            g2.drawString((value*100/total)+"%",tx-10,ty+12);
            return start+angle;
        }
    }

    class LinePanel extends JPanel {
        private final List<Integer> data=new ArrayList<>();
        LinePanel(String name){
            try(Connection c=DriverManager.getConnection("jdbc:sqlite:janken.db");
                PreparedStatement ps=c.prepareStatement("SELECT reaction_ms FROM game_logs WHERE name=? ORDER BY id")){
                ps.setString(1,name);
                ResultSet rs=ps.executeQuery();
                while(rs.next()) data.add(rs.getInt(1));
            }catch(Exception e){ e.printStackTrace(); }
        }
        @Override
        protected void paintComponent(Graphics g){
            super.paintComponent(g);
            if(data.size()<2) return;
            Graphics2D g2=(Graphics2D)g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

            int left=50,bottom=getHeight()-40;
            int width=getWidth()-80,height=getHeight()-80;
            int max=data.stream().max(Integer::compare).orElse(1);
            int avg=(int)data.stream().mapToInt(i->i).average().orElse(0);

            // グリッド
            g2.setColor(Color.LIGHT_GRAY);
            for(int i=0;i<=5;i++){
                int y=bottom-i*height/5;
                g2.drawLine(left,y,left+width,y);
            }

            // 平均線
            int avgY=bottom-avg*height/max;
            g2.setColor(Color.ORANGE);
            g2.drawLine(left,avgY,left+width,avgY);
            g2.drawString("AVG "+avg+"ms",left+width-70,avgY-5);

            // 折れ線
            g2.setColor(Color.BLUE);
            int prevX=left,prevY=bottom-data.get(0)*height/max;
            for(int i=1;i<data.size();i++){
                int x=left+i*width/(data.size()-1);
                int y=bottom-data.get(i)*height/max;
                g2.drawLine(prevX,prevY,x,y);
                g2.fillOval(x-3,y-3,6,6);
                prevX=x; prevY=y;
            }
        }
    }

    class PlayerSearchFrame extends JFrame {
        PlayerSearchFrame(){
            setTitle("プレイヤー検索");
            setSize(420,320);
            setLocationRelativeTo(null);
            String name=JOptionPane.showInputDialog(this,"プレイヤー名を入力してください");
            JTextArea area=new JTextArea();
            area.setEditable(false);
            area.setFont(new Font("Meiryo",Font.PLAIN,14));
            if(name==null||name.isBlank()){
                area.setText("名前が入力されていません。");
                add(new JScrollPane(area));
                return;
            }
            int count=ScoreManager.getPlayCount(name);
            double avg=ScoreManager.getAverageReaction(name);
            String hands=ScoreManager.getHandSummary(name);
            String msg="【"+name+" の履歴データ】\n\n" +
                    "総プレイ回数："+count+"回\n" +
                    "平均反応時間："+String.format("%.1f",avg)+" ms\n\n" +
                    "手の使用回数：\n"+hands+"\n\n" +
                    "――――――――――――――\n"+ScoreManager.getRanking();
            area.setText(msg);
            add(new JScrollPane(area));
        }
    }
}