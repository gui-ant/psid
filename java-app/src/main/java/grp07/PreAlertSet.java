package grp07;

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
    public synchronized void addPreAlert (Timestamp insertion, CultureParams param) {
        susParams.put(insertion, param);
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

    // apagar registos com mais de 30 segundos
    private void deleteOldAlerts() {
        Map<Timestamp, CultureParams> newSusParams = new HashMap<>();
        Timestamp curr = Timestamp.from(Instant.now());
        curr.setTime(curr.getTime() + (30*1000));

        for (Timestamp time : susParams.keySet()) {
            if (time.before(curr)) {
                CultureParams par = susParams.get(time);
                newSusParams.put(time, par);
            }
        }
        susParams = newSusParams;
    }

}
