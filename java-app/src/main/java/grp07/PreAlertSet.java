package grp07;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

// APENAS A THREAD SUPERVISORA "USA" O PREALERTSET
public class PreAlertSet {
    private final List<TimeParameterPair> susParams;  //esta lista tem os parametros INDIVIDUAIS
    private Hashtable<Long, List<CultureParams>> allParams;  //esta lista tem os parametros COMPOSTOS (ir buscar ao SQLSender)
    private boolean altered;

    public PreAlertSet (Hashtable<Long, List<CultureParams>> allParams) {
        this.allParams = allParams;
        this.susParams = new ArrayList<>();
        this.altered = true;
    }

    //alterado por SQLSender
    public void setAllParams (Hashtable<Long, List<CultureParams>> allParams) {
        this.allParams = allParams;
    }

    //populado por cada Thread de sensor
    public synchronized void addPreAlert (Timestamp insertion, CultureParams param, boolean isAlert) {
        deleteParamOcc(param);
        if (isAlert) {
            susParams.add(new TimeParameterPair(insertion, param));
        }
        altered = true;
        notifyAll();
    }

    //executado pelo supervisor
    public synchronized void analyse() throws InterruptedException {
        while (! altered) {
            System.out.println("Supervisor vai dormir");
            wait();
        }
        System.out.println("Supervisor não dorme!");
        altered = false;
        deleteOldAlerts();

        ArrayList<CultureParams> sus = (ArrayList<CultureParams>) susToParameterArray();
        for (Long id : allParams.keySet()) {
            List<CultureParams> paramSet = allParams.get(id);
            if (sus.containsAll(paramSet)) {
                // TODO - enviar Alerta
                Alert alert = new Alert(0, id, Timestamp.from(Instant.now()), "wow mano, uma mensagem");
            }
        }
    }

    private void deleteParamOcc (CultureParams param) {
        susParams.removeIf(pair -> pair.getParam().isEqual(param));
    }

    // apagar registos com mais de 30 segundos
    private void deleteOldAlerts() {
        Timestamp curr = Timestamp.from(Instant.now());
        curr.setTime(curr.getTime() + (30*1000));
        susParams.removeIf(pair -> pair.getTime().after(curr));
    }

    private List<CultureParams> susToParameterArray() {
        List<CultureParams> arr = new ArrayList<>();
        for (TimeParameterPair pair : susParams) {
            arr.add(pair.getParam());
        }
        return arr;
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

        CultureParams p11 = new CultureParams();
        p11.setSensorType("h");
        p11.setValMax(5);
        p11.setValMin(2);
        p11.setTolerance(15);
        p11.setCulture(c);

        CultureParams p2 = new CultureParams();
        p2.setSensorType("t");
        p2.setValMax(10);
        p2.setValMin(5);
        p2.setTolerance(15);
        p2.setCulture(c);

        CultureParams p3 = new CultureParams();
        p3.setSensorType("l");
        p3.setValMax(50);
        p3.setValMin(49);
        p3.setTolerance(15);
        p3.setCulture(c);

        List<CultureParams> list = new ArrayList<>();
        list.add(p1);
        list.add(p3);

        Hashtable<Long, List<CultureParams>> todosParams = new Hashtable<>();
        todosParams.put(1L, list);

        PreAlertSet pas = new PreAlertSet(todosParams);
        pas.addPreAlert(Timestamp.from(Instant.now()), p1, true);
        //pas.addPreAlert(Timestamp.from(Instant.now()), p2, true);
        //pas.addPreAlert(Timestamp.from(Instant.now()), p1, true);
        pas.addPreAlert(Timestamp.from(Instant.now()), p3, true);

//        try {
//            Thread.sleep(35000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


        try {
            pas.analyse();
        } catch (InterruptedException e) {
            System.out.println("ERRO!!!");
        }
    }
*/

    private static class TimeParameterPair {
        private final Timestamp time;
        private final CultureParams param;

        public TimeParameterPair(Timestamp time, CultureParams param) {
            this.time = time;
            this.param = param;
        }

        public Timestamp getTime() {
            return time;
        }

        public CultureParams getParam() {
            return param;
        }
    }

}
