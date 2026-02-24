package ui;

import javax.swing.*;
import java.util.Random;

public class CpuAnimator {

    private final Timer timer;
    private final ImageIcon[] icons;
    private final Random random;
    private final JLabel target;

    private int currentHand;

    public CpuAnimator(JLabel target, ImageIcon[] icons, Random random, int intervalMs) {
        this.target = target;
        this.icons = icons;
        this.random = random;

        // 初期値をランダムに
        currentHand = random.nextInt(3);
        target.setIcon(icons[currentHand]);

        this.timer = new Timer(intervalMs, e -> {
            currentHand = random.nextInt(3);
            target.setIcon(icons[currentHand]);
        });
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    public boolean isRunning() {
        return timer.isRunning();
    }

    public int getCurrentHand() {
        return currentHand;
    }
}