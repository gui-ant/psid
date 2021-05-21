package grp07;

import common.IniConfig;

import java.sql.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

// APENAS A THREAD SUPERVISORA "USA" O PREALERTSET
public class PreAlertSet extends IniConfig {
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASS = "";

    private final List<TimeParameterPair> susParams;  //esta lista tem os parametros INDIVIDUAIS
    private Hashtable<Long, List<MySqlData.CultureParams>> allParams;  //esta lista tem os parametros COMPOSTOS (ir buscar ao SQLSender)
    private boolean altered;

    public PreAlertSet (Hashtable<Long, List<MySqlData.CultureParams>> allParams) {
        super("config.ini");
        this.allParams = allParams;
        this.susParams = new ArrayList<>();
        this.altered = true;
    }

    //alterado por SQLSender
    public void setAllParams (Hashtable<Long, List<MySqlData.CultureParams>> allParams) {
        this.allParams = allParams;
    }

    //populado por cada Thread de sensor
    public synchronized void addPreAlert (Timestamp insertion, MySqlData.CultureParams param, boolean isAlert) {
//        System.err.println("PreAlertSet: alerta adicionado!");
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

        ArrayList<MySqlData.CultureParams> sus = (ArrayList<MySqlData.CultureParams>) susToParameterArray();
        for (Long id : allParams.keySet()) {
            List<MySqlData.CultureParams> paramSet = allParams.get(id);
            if (sus.containsAll(paramSet)) {

                String msg = buildAlertMessage(paramSet);
                Alert alert = new Alert(0, id, 0, 0, Timestamp.from(Instant.now()), msg);

                try {
                    Connection mysql = DriverManager.getConnection(getConfig("mysql","cloud_uri"), MYSQL_USER, MYSQL_PASS);
                    String sql = "INSERT INTO alerts (parameter_set_id, created_at, message) VALUES (?, ?, ?)";

                    PreparedStatement statement = mysql.prepareStatement(sql);
                    statement.setLong(1, alert.getParameterSetId());
                    statement.setTimestamp(2, alert.getCreatedAt());
                    statement.setString(3, alert.getMsg());
                    statement.execute();

                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                    System.err.println("Alerta rejeitado. Já existe alerta anterior para a mesma parametrização nos últimos 15 min.");
                }
            }
        }
    }

    private String buildAlertMessage(List<MySqlData.CultureParams> singleSet) {
        MySqlData.Culture culture = singleSet.get(0).getCulture();
        StringBuilder sb = new StringBuilder();
        sb.append("Atencao à cultura " + culture.getName() + ", na zona " + culture.getZone().getId() + "! O(s) sensor(es) ");
        for (MySqlData.CultureParams p : singleSet) {
            sb.append(p.getSensorType() + " ");
        }
        sb.append("apresentam valores fora dos limites definidos.");

        return sb.toString();
    }

    private void deleteParamOcc (MySqlData.CultureParams param) {
        susParams.removeIf(pair -> pair.getParam().isEqual(param));
    }

    // apagar registos com mais de 30 segundos
    private void deleteOldAlerts() {
        Timestamp curr = Timestamp.from(Instant.now());
        curr.setTime(curr.getTime() + (30*1000));
        susParams.removeIf(pair -> pair.getTime().after(curr));
    }

    private List<MySqlData.CultureParams> susToParameterArray() {
        List<MySqlData.CultureParams> arr = new ArrayList<>();
        for (TimeParameterPair pair : susParams) {
            arr.add(pair.getParam());
        }
        return arr;
    }

/*
    public static void main(String[] args) {
        MySqlData.User u = new MySqlData.User(3);
        u.setEmail("mail");
        u.setName("name");
        u.setRole(2);
        MySqlData.Zone z = new MySqlData.Zone(6);
        z.setTemperature(20);
        z.setHumidity(21);
        z.setLight(22);
        MySqlData.Culture c = new MySqlData.Culture(5L);
        c.setName("cultura");
        c.setZone(z);
        c.setManager(u);
        c.setState(true);
        MySqlData.CultureParams p1 = new MySqlData.CultureParams();
        p1.setSensorType("h");
        p1.setValMax(5);
        p1.setValMin(2);
        p1.setTolerance(1);
        p1.setCulture(c);
        MySqlData.CultureParams p11 = new MySqlData.CultureParams();
        p11.setSensorType("h");
        p11.setValMax(5);
        p11.setValMin(2);
        p11.setTolerance(15);
        p11.setCulture(c);
        MySqlData.CultureParams p2 = new MySqlData.CultureParams();
        p2.setSensorType("t");
        p2.setValMax(10);
        p2.setValMin(5);
        p2.setTolerance(15);
        p2.setCulture(c);
        MySqlData.CultureParams p3 = new MySqlData.CultureParams();
        p3.setSensorType("l");
        p3.setValMax(50);
        p3.setValMin(49);
        p3.setTolerance(15);
        p3.setCulture(c);
        List<MySqlData.CultureParams> list = new ArrayList<>();
        list.add(p1);
        list.add(p3);
        Hashtable<Long, List<MySqlData.CultureParams>> todosParams = new Hashtable<>();
        todosParams.put(1L, list);
        PreAlertSet pas = new PreAlertSet(todosParams);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pas.addPreAlert(Timestamp.from(Instant.now()), p1, true);
        for (TimeParameterPair p : pas.susParams) {
            System.out.println(p.getParam().getSensorType() + " " + p.getTime());
        }
        System.out.println("------------------------------");
        //pas.addPreAlert(Timestamp.from(Instant.now()), p2, true);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pas.addPreAlert(Timestamp.from(Instant.now()), p1, true);
        for (TimeParameterPair p : pas.susParams) {
            System.out.println(p.getParam().getSensorType() + " " + p.getTime());
        }
        System.out.println("------------------------------");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pas.addPreAlert(Timestamp.from(Instant.now()), p3, true);
        for (TimeParameterPair p : pas.susParams) {
            System.out.println(p.getParam().getSensorType() + " " + p.getTime());
        }
        System.out.println("------------------------------");
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


    private class TimeParameterPair {
        private final Timestamp time;
        private final MySqlData.CultureParams param;

        public TimeParameterPair(Timestamp time, MySqlData.CultureParams param) {
            this.time = time;
            this.param = param;
        }

        public Timestamp getTime() {
            return time;
        }

        public MySqlData.CultureParams getParam() {
            return param;
        }
    }

}