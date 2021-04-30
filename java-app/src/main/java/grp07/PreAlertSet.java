package grp07;

import com.mysql.cj.protocol.x.SyncFlushDeflaterOutputStream;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

// APENAS A THREAD SUPERVISORA "USA" O PREALERTSET
public class PreAlertSet {
    private Map<Timestamp, CultureParams> susParams;  //esta lista tem os parametros INDIVIDUAIS
    private Hashtable<Long, List<CultureParams>> allParams;  //esta lista tem os parametros COMPOSTOS (ir buscar ao SQLSender)
    private boolean altered;

    public PreAlertSet (Hashtable<Long, List<CultureParams>> allParams) {
        this.allParams = allParams;
        this.susParams = new HashMap<>();
        this.altered = true;
    }

    //alterado por SQLSender
    public void setAllParams (Hashtable<Long, List<CultureParams>> allParams) {
        this.allParams = allParams;
    }

    //populado por cada Thread de sensor
    public synchronized void addPreAlert (Timestamp insertion, CultureParams param, boolean isAlert) {
        System.out.println("addPreAlert: " + param.getSensorType());
        deleteParamOcc(param);
        if (isAlert) {
            susParams.put(insertion, param);
        }
        altered = true;
        notifyAll();
    }

    //executado pelo supervisor
    public synchronized void analyse() throws InterruptedException {
        while (! altered) {
            System.out.println("Dentro do analyse, antes do wait");
            wait();
            System.out.println("Dentro do analyse, depois do wait");
        }
        altered = false;
        deleteOldAlerts();

        for (Long id : allParams.keySet()) {
            List<CultureParams> paramSet = allParams.get(id);
            if (susParams.values().containsAll(paramSet)) {
                // REVER ISSO!!!
                Alert alert = new Alert(0, id, Timestamp.from(Instant.now()), "wow mano, uma mensagem");
            }
        }
    }

    private void deleteParamOcc (CultureParams param) {
        System.out.println("deleteParamOcc -> " + param.getSensorType());
        for(Iterator<Map.Entry<Timestamp, CultureParams>> it = susParams.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Timestamp, CultureParams> entry = it.next();
            if(entry.getValue().isEqual(param)) {
                System.out.println("deleParaOcc, sao iguais");
                it.remove();
                break;
            }
        }
    }

    // apagar registos com mais de 30 segundos
    private void deleteOldAlerts() {
        Timestamp curr = Timestamp.from(Instant.now());
        curr.setTime(curr.getTime() + (30*1000));

        for(Iterator<Map.Entry<Timestamp, CultureParams>> it = susParams.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Timestamp, CultureParams> entry = it.next();
            if(entry.getKey().after(curr)) {
                it.remove();
            }
        }
    }

/*
    public static void main(String[] args) {
        Hashtable<Long, List<CultureParams>> allParams = new Hashtable<>();
        PreAlertSet pas = new PreAlertSet(allParams);

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
        p1.setTolerance(15);
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
        p2.setValMin(4);
        p2.setTolerance(30);
        p2.setCulture(c);

        CultureParams p22 = new CultureParams();
        p22.setSensorType("t");
        p22.setValMax(10);
        p22.setValMin(4);
        p22.setTolerance(30);
        p22.setCulture(c);


//        try {
//            pas.analyse();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


        pas.addPreAlert(Timestamp.from(Instant.now()), p1, true);
        System.out.println("susParams size " + pas.susParams.size());

        System.out.println();
        pas.addPreAlert(Timestamp.from(Instant.now()), p11, true);
        System.out.println("susParams size " + pas.susParams.size());

        System.out.println();
        pas.addPreAlert(Timestamp.from(Instant.now()), p2, true);
        System.out.println("susParams size " + pas.susParams.size());

        System.out.println();
        pas.addPreAlert(Timestamp.from(Instant.now()), p22, false);
        System.out.println("susParams size " + pas.susParams.size());

    }
*/

}
