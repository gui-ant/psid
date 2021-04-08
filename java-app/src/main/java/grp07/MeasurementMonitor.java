package grp07;

import java.util.Hashtable;

public class MeasurementMonitor extends Thread {
    Hashtable buffer;

    MeasurementMonitor(Hashtable buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        super.run();
    }
}
