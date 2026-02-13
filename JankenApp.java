import java.util.Random;
import java.util.Scanner;

public class JankenApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        String[] hands = {"グー", "チョキ", "パー"};

        System.out.println("じゃんけんぽん！");
        System.out.print("0:グー 1:チョキ 2:パー → ");

        int yuma = scanner.nextInt();
        int PPC = random.nextInt(3);

        System.out.println("あなた: " + hands[yuma]);
        System.out.println("CPU: " + hands[PPC]);

        if (yuma == PPC) {
            System.out.println("あいこ！");
        } else if ((yuma == 0 && PPC == 1) ||
                   (yuma == 1 && PPC == 2) ||
                   (yuma == 2 && PPC == 0)) {
            System.out.println("あなたの勝ち！");
        } else {
            System.out.println("あなたの負け！");
        }

        scanner.close();
    }
}

