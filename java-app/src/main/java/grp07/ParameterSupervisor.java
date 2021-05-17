package grp07;

public class ParameterSupervisor extends Thread {
    private final PreAlertSet preAlertSet;


    public ParameterSupervisor(PreAlertSet preAlertSet) {
        this.preAlertSet = preAlertSet;
    }

    public void run() {
        System.out.println("Supervisor started");
        while (true) {
            try {
                preAlertSet.analyse();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

/*
    public static void main(String[] args) {
        User u = new User(3);
        u.setEmail("mail");
        u.setName("name");
        u.setRole(2);

        Zone z = new Zone(6);
        z.setTemperature(20);
        z.setHumidity(21);
        z.setLight(22);

        Culture c = new Culture(5L);
        c.setName("cultura");
        c.setZone(z);
        c.setManager(u);
        c.setState(true);

        CultureParams p1 = new CultureParams();
        p1.setSensorType("h");
        p1.setValMax(5);
        p1.setValMin(2);
        p1.setTolerance(1);
        p1.setCulture(c);

        List<CultureParams> list = new ArrayList<>();
        list.add(p1);

        Hashtable<Long, List<CultureParams>> todosParams = new Hashtable<>();
        todosParams.put(1L, list);

        PreAlertSet pas = new PreAlertSet(todosParams);
        pas.addPreAlert(Timestamp.from(Instant.now()), p1, true);

        ParameterSupervisor ps = new ParameterSupervisor(pas);
        ps.start();

        while (true) {
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pas.addPreAlert(Timestamp.from(Instant.now()), p1, true);
        }

    }
*/
}
