import java.util.Random;
import java.util.Scanner;

public class JankenApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        String[] hands = {"グー", "チョキ", "パー"};

        System.out.println("じゃんけんぽん！");
        System.out.print("0:グー 1:チョキ 2:パー → ");

        int user = scanner.nextInt();
        int cpu = random.nextInt(3);

        System.out.println("あなた: " + hands[user]);
        System.out.println("CPU: " + hands[cpu]);

        if (user == cpu) {
            System.out.println("あいこ！");
        } else if ((user == 0 && cpu == 1) ||
                   (user == 1 && cpu == 2) ||
                   (user == 2 && cpu == 0)) {
            System.out.println("あなたの勝ち！");
        } else {
            System.out.println("あなたの負け！");
        }

        scanner.close();
    }
}
