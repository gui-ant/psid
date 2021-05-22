package grp07;

import common.IniConfig;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

// CADA THREAD TEM UM PARAMANALYSER!!!
public class ParamAnalyser extends IniConfig {
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASS = "";

    private final PreAlertSet preAlertCompiler;
    private List<MySqlData.CultureParams> paramList;  //lista de parametros INDIVIDUAIS de um dado sensor
    private final List<Measurement> measurements;
    private int maxTolerance;
    private final long insertionRate;  //frequencia a que as medidas são geradas
    //    private int numCycles = 10;
    private final int numCycles = 3;

    public ParamAnalyser(PreAlertSet preAlertCompiler, ArrayList<MySqlData.CultureParams> paramList, long insertionRate) {
        super("config.ini");
        this.preAlertCompiler = preAlertCompiler;
        this.paramList = paramList;
        this.measurements = new LinkedList<>();
        this.maxTolerance = 0;
        this.insertionRate = insertionRate;

        double r = Math.random();
        for (MySqlData.CultureParams p : paramList) {
            System.err.println(r + " Parametro no analyser: " + p.getParamId());
        }
        System.err.println("--------------------------");
    }

    public void addMeasurement(Measurement measure) {
        this.measurements.add(0, measure);
    }

    public void setParamList(ArrayList<MySqlData.CultureParams> paramList) {
        this.paramList = paramList;
    }

    public void analyseParameters() {
//        System.err.println("analyse");

        for (MySqlData.CultureParams param : paramList) {
            boolean isSus = isSuspect(param);
            if (isSus) {
                System.err.println("::::::::::::::::::::::::::::::::::::::::::::ADICIONOU ALERT TRUE:::::::::::::::::::::::::::::::::::::::::::::::::::::");
                preAlertCompiler.addPreAlert(Timestamp.from(Instant.now()), param, true);
            }
            if (!isSus) {
                System.err.println("::::::::::::::::::::::::::::::::::::::::::::ADICIONOU ALERT FALSE:::::::::::::::::::::::::::::::::::::::::::::::::::::");
                preAlertCompiler.addPreAlert(Timestamp.from(Instant.now()), param, false);
            }
            if (param.getTolerance() > maxTolerance) {
                maxTolerance = param.getTolerance();
            }
        }
        trimMeasureList();

    }

    private boolean isSuspect(MySqlData.CultureParams param) {
//        System.err.println("ParamAnalyser: dentro do isSuspect");
        //controlar subidas/descidas constantes em parametros sem tolerancia
        if (param.getTolerance() == 0) {
            Alert alert = zeroToleranceAnalyser(param);
            if (alert != null) {
                try {
                    Connection mysql = DriverManager.getConnection(getConfig("mysql", "local_uri"), MYSQL_USER, MYSQL_PASS);
                    String sql = "INSERT INTO alerts (param_id, created_at, message) VALUES (?, ?, ?)";

                    PreparedStatement statement = mysql.prepareStatement(sql);
                    statement.setLong(1, alert.getParamId());
                    statement.setTimestamp(2, alert.getCreatedAt());
                    statement.setString(3, alert.getMsg());
                    statement.execute();

                } catch (SQLException throwables) {
                    System.err.println("Alerta rejeitado. Já existe alerta anterior para a mesma parametrização nos últimos 5 min.");
                }
            }
        }
        int tolerance = param.getTolerance();
        Timestamp headTime = measurements.get(0).getTimestamp();
        for (Measurement measure : measurements) {
            if (measure.getRoundValue() < param.getValMax() && measure.getRoundValue() > param.getValMin()) {
                return false;
            }

            //obriga a comparar APÓS limite de tolerancia
            if ((headTime.getTime() - measure.getTimestamp().getTime()) / 1000 > tolerance) {
                break;
            }
        }
        return true;
    }

    private Alert zeroToleranceAnalyser(MySqlData.CultureParams param) {
        boolean constantRise = constantRise(param);
        boolean constantFall = constantFall(param);
        Alert alert = null;
        if (constantRise) {

            StringBuilder sb = new StringBuilder();
            sb.append("Atencao na cultura " + param.getCulture().getName() + ", na zona " + param.getCulture().getZone().getId() + ".");
            sb.append(" O sensor " + param.getSensorType());
            sb.append(" detetou uma subida constante perto dos limites definidos, há " + numCycles + " medidas consecutivas");

            alert = new Alert(0, 0, 0, param.getParamId(), Timestamp.from(Instant.now()), sb.toString());
        }
        if (constantFall) {

            StringBuilder sb = new StringBuilder();
            sb.append("Atencao na cultura " + param.getCulture().getName() + ", na zona " + param.getCulture().getZone().getId() + ".");
            sb.append(" O sensor " + param.getSensorType());
            sb.append(" detetou uma descida constante perto dos limites definidos, há " + numCycles + " medidas consecutivas");

            alert = new Alert(0, 0, 0, param.getParamId(), Timestamp.from(Instant.now()), sb.toString());
        }
        return alert;
    }

    private boolean constantRise(MySqlData.CultureParams param) {
        //sempre a descer, dentro de limites, durante 7 medidas
        double diff = (param.getValMax() - param.getValMin()) * 0.3;
        double minLim = param.getValMax() - diff;

        double val = measurements.get(0).getRoundValue();

        if (val > param.getValMax() || val < minLim || measurements.size() < numCycles) {
            return false;
        }

        for (int ind = 1; ind < numCycles; ind++) {
            double newval = measurements.get(ind).getRoundValue();
            if (newval >= val || newval > param.getValMax() || newval < minLim) {
                return false;
            }
            val = newval;
        }
        return true;
    }

    private boolean constantFall(MySqlData.CultureParams param) {
        //sempre a subir, dentro de limites, durante 7 medidas
        double diff = (param.getValMax() - param.getValMin()) * 0.3;
        double maxLim = param.getValMin() + diff;

        double val = measurements.get(0).getRoundValue();

        if (val > maxLim || val < param.getValMin() || measurements.size() < numCycles) {
            return false;
        }

        for (int ind = 1; ind < numCycles; ind++) {
            double newval = measurements.get(ind).getRoundValue();
            if (newval <= val || newval > maxLim || newval < param.getValMin()) {
                return false;
            }
            val = newval;
        }
        return true;
    }

    private void trimMeasureList() {
        Timestamp maxThreshold = subtractSecondsThreshold(measurements.get(0).getTimestamp(), maxTolerance, insertionRate);
        measurements.removeIf(m -> m.getTimestamp().compareTo(maxThreshold) < 0);
    }

    private Timestamp subtractSecondsThreshold(Timestamp time, int tolerance, long rate) {
        time.setTime(time.getTime() - (tolerance + 6 * rate) * 1000);
        return time;
    }

    public List<MySqlData.CultureParams> getParamList() {
        return paramList;
    }

/*
    public static void main (String[] args) {
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
        p1.setTolerance(0);
        p1.setCulture(c);

        Measurement mea1 = new Measurement();
        mea1.setZone("z1");
        mea1.setSensor("t");
        mea1.setDate("2021-05-02 00:06:30");
        mea1.setValue("2.1");

        Measurement mea2 = new Measurement();
        mea2.setZone("z1");
        mea2.setSensor("t");
        mea2.setDate("2021-05-02 00:06:31");
        mea2.setValue("2.8");

        Measurement mea3 = new Measurement();
        mea3.setZone("z1");
        mea3.setSensor("t");
        mea3.setDate("2021-05-02 00:06:32");
        mea3.setValue("2.7");

        Measurement mea4 = new Measurement();
        mea4.setZone("z1");
        mea4.setSensor("t");
        mea4.setDate("2021-05-02 00:06:33");
        mea4.setValue("2.6");

        ArrayList<MySqlData.CultureParams> paramList = new ArrayList<>();
        paramList.add(p1);
//        paramList.add(p2);

        Hashtable<Long, List<MySqlData.CultureParams>> todosParams = new Hashtable<>();
        todosParams.put(1L, paramList);

        PreAlertSet pas = new PreAlertSet(todosParams);

        ParamAnalyser pa = new ParamAnalyser(pas, paramList, 5);

        pa.addMeasurement(mea1);
        pa.addMeasurement(mea2);
        pa.addMeasurement(mea3);
        pa.addMeasurement(mea4);

        pa.analyseParameters();
    }
*/
}
