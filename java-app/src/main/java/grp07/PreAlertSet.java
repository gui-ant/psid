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
        for (Long k : allParams.keySet()) {
            List<MySqlData.CultureParams> parametros = allParams.get(k);
            for (MySqlData.CultureParams p : parametros) {
                System.err.println("PreAlertSet: setId - " + p.getSetId() + " paramId - " + p.getParamId());
                System.err.println("-----------------------");
            }
            System.err.println("++++++++++++++++++++++++++++++++++++++");
        }
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
            System.err.println("------------------------------------------------VAI COMEÇAR OS COMPOSTOS: " + allParams.size() + "---------------------------------------------------");
            List<MySqlData.CultureParams> paramSet = allParams.get(id);

            System.err.println("----------------------------------Dentro dOS COMPOSTOS: " + paramSet.size() + "---------------------------------------------------");
            System.err.println("sus: \n");
            for(MySqlData.CultureParams a : sus){
                System.out.println(a.getParamId() + " " + a.getSensorType());
            }
            System.err.println("**********************************Dentro dOS COMPOSTOS - size do sus: " + sus.size() + "---------------------------------------------------");
//            if (sus.containsAll(paramSet)) {
            if (susContainsAllParams(sus, paramSet)) {

                System.err.println("+++++++++++++++++++++ALERTA COMPOSTO!!!+++++++++++++++++++++++++");

                String msg = buildAlertMessage(paramSet);
                Alert alert = new Alert(0, id, 0, 0, Timestamp.from(Instant.now()), msg);

                try {
                    Connection mysql = DriverManager.getConnection(getConfig("mysql","local_uri"), MYSQL_USER, MYSQL_PASS);
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

    private boolean susContainsAllParams(List<MySqlData.CultureParams> sus, List<MySqlData.CultureParams> paramSet) {
        List<Long> susIdList = new ArrayList<>();
        List<Long> paramSetIdList = new ArrayList<>();
        for (MySqlData.CultureParams p1 : sus) {
            susIdList.add(p1.getParamId());
        }
        for (MySqlData.CultureParams p2 : paramSet) {
            paramSetIdList.add(p2.getParamId());
        }
        return susIdList.containsAll(paramSetIdList);
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
        Timestamp limit = Timestamp.from(Instant.now());
        limit.setTime(limit.getTime() - 30000);
        susParams.removeIf(a -> a.getTime().before(limit));
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
        System.err.println("begin: " + Timestamp.from(Instant.now()));
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
        Timestamp t1 = Timestamp.from(Instant.now());
        t1.setTime(t1.getTime() - 45000);
//        t1.setTime(t1.getTime() + 45000);
        pas.addPreAlert(t1, p1, true);
//        for (TimeParameterPair p : pas.susParams) {
//            System.out.println(p.getParam().getSensorType() + " " + p.getTime());
//        }
//        System.out.println("------------------------------");
        Timestamp t2 = Timestamp.from(Instant.now());
        t2.setTime(t2.getTime() - 45000);
//        t2.setTime(t2.getTime() + 45000);
        pas.addPreAlert(t2, p2, true);


        Timestamp t3 = Timestamp.from(Instant.now());
        t3.setTime(t3.getTime() - 45000);
//        t3.setTime(t3.getTime() + 45000);
        pas.addPreAlert(t3, p1, true);
//        for (TimeParameterPair p : pas.susParams) {
//            System.out.println(p.getParam().getSensorType() + " " + p.getTime());
//        }
//        System.out.println("------------------------------");
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        Timestamp t4 = Timestamp.from(Instant.now());
        t4.setTime(t4.getTime() - 45000);
//        t4.setTime(t4.getTime() + 45000);
        pas.addPreAlert(t4, p3, true);
//        for (TimeParameterPair p : pas.susParams) {
//            System.out.println(p.getParam().getSensorType() + " " + p.getTime());
//        }
//        System.out.println("------------------------------");
////        try {
////            Thread.sleep(35000);
////        } catch (InterruptedException e) {
////            e.printStackTrace();
////        }
//        try {
//            pas.analyse();
//        } catch (InterruptedException e) {
//            System.out.println("ERRO!!!");
//        }
        for (TimeParameterPair p : pas.susParams) {
            System.out.println("ANTES: " + p.getParam().getSensorType() + " " + p.getTime());
        }

//        Timestamp a = Timestamp.from(Instant.now());
//        System.err.println(a.getTime());

        pas.deleteOldAlerts();

        for (TimeParameterPair p : pas.susParams) {
            System.out.println("DEPOIS: " + p.getParam().getSensorType() + " " + p.getTime());
        }

//        Timestamp b = Timestamp.from(Instant.now());
//        System.err.println(b.getTime());

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