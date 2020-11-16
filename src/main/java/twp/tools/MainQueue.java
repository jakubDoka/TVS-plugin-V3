package twp.tools;

import java.util.ArrayList;

public class MainQueue {
    ArrayList<Runnable> tasks = new ArrayList<>();

    public synchronized void post(Runnable run) {
        tasks.add(run);
    }

    public synchronized void run() {
        for(Runnable t : tasks) {
            t.run();
        }

        tasks.clear();
    }
}
