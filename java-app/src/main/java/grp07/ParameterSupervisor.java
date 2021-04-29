package grp07;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class ParameterSupervisor extends Thread {
    private PreAlertSet preAlertSet;


    public ParameterSupervisor(PreAlertSet preAlertSet) {
        this.preAlertSet = preAlertSet;
    }

    public void run() {
        System.out.println("Supervisor started");
        while (true) {
            try {
                System.out.println("Supervisor dentro do while/try");
                preAlertSet.analyse();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
