package ui;

import javax.swing.*;
import java.util.Random;

public class CpuAnimator {
    private final Timer timer;

    public CpuAnimator(JLabel target, ImageIcon[] icons, Random random, int intervalMs) {
        this.timer = new Timer(intervalMs, e -> target.setIcon(icons[random.nextInt(3)]));
    }

    public void start() { timer.start(); }
    public void stop() { timer.stop(); }

    public boolean isRunning() { return timer.isRunning(); }
}