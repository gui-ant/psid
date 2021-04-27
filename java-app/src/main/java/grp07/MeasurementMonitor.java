package grp07;

import java.util.Hashtable;

public class MeasurementMonitor extends Thread {
    Hashtable buffer;

    MeasurementMonitor(Hashtable buffer) {
        this.buffer = buffer;
    }

    @Override
    public void run() {
        while (true){
            try {
                wait();
                System.out.println("");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
