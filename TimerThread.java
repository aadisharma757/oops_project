import javafx.application.Platform;
import javafx.scene.control.Label;

// Multithreading: the countdown runs on its own thread so the window stays responsive.
public class TimerThread extends Thread {

    private int secondsLeft;
    private Label timerLabel;
    private Runnable onTimeUp;            // what to do when time reaches 0
    private volatile boolean running = true;

    public TimerThread(int seconds, Label timerLabel, Runnable onTimeUp) {
        this.secondsLeft = seconds;
        this.timerLabel = timerLabel;
        this.onTimeUp = onTimeUp;
    }

    @Override
    public void run() {
        showTime();
        while (running && getSecondsLeft() > 0) {
            try {
                Thread.sleep(1000);      // wait one second
            } catch (InterruptedException e) {
                return;                  // timer was stopped
            }
            tick();
            showTime();
        }
        if (running) {
            Platform.runLater(onTimeUp); // time is up, tell the UI thread
        }
    }

    // synchronized: the UI thread and this thread both change secondsLeft.
    private synchronized void tick() {
        if (secondsLeft > 0) {
            secondsLeft--;
        }
    }

    public synchronized int getSecondsLeft() {
        return secondsLeft;
    }

    // Called by the game when the player answers wrongly.
    public synchronized void subtractTime(int seconds) {
        secondsLeft = Math.max(0, secondsLeft - seconds);
        showTime();
    }

    public void stopTimer() {
        running = false;
        interrupt();
    }

    // JavaFX controls may only be changed on the UI thread, so use Platform.runLater.
    private void showTime() {
        int s = getSecondsLeft();
        Platform.runLater(() ->
            timerLabel.setText(String.format("TIME LEFT: %02d:%02d", s / 60, s % 60)));
    }
}
