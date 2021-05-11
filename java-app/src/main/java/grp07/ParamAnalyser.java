package grp07;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

import static java.lang.Thread.sleep;

// CADA THREAD TEM UM PARAMANALYSER!!!
public class ParamAnalyser {
    private PreAlertSet preAlertCompiler;
    private List<CultureParams> paramList;  //lista de parametros INDIVIDUAIS de um dado sensor
    private List<Measurement> measurements;
    private int maxTolerance;
    private int insertionRate;  //frequencia a que as medidas são geradas
    //    private int numCycles = 10;
    private int numCycles = 3;

    public ParamAnalyser(PreAlertSet preAlertCompiler, ArrayList<CultureParams> paramList, int insertionRate) {
        this.preAlertCompiler = preAlertCompiler;
        this.paramList = paramList;
        this.measurements = new LinkedList<>();
        this.maxTolerance = 0;
        this.insertionRate = insertionRate;
    }

    public void addMeasurement(Measurement measure) {
        this.measurements.add(0, measure);
    }

    public void setParamList(ArrayList<CultureParams> paramList) {
        this.paramList = paramList;
    }

    public void analyseParameters() {
        for (CultureParams param : paramList) {
            boolean isSus = isSuspect(param);
            if (isSus) {
                preAlertCompiler.addPreAlert(Timestamp.from(Instant.now()), param, true);
            }
            if (!isSus) {
                preAlertCompiler.addPreAlert(Timestamp.from(Instant.now()), param, false);
            }
            if (param.getTolerance() > maxTolerance) {
                maxTolerance = param.getTolerance();
            }
        }
        trimMeasureList();
    }

    private boolean isSuspect(CultureParams param) {
        //controlar subidas/descidas constantes em parametros sem tolerancia
        if (param.getTolerance() == 0) {
            // TODO - ele lança ALERTA, ou passa o alerta para o PreAlertCompiler para ser logo enviado para o MySQL???
            Alert alert = zeroToleranceAnalyser(param);
            if (alert != null) {
                // ENVIAR ALERTA!!!
            }
        }
        int tolerance = param.getTolerance();
        Timestamp headTime = measurements.get(0).getTimestamp();
        for (Measurement measure : measurements) {
            if (Double.parseDouble(measure.getValue()) < param.getValMax() && Double.parseDouble(measure.getValue()) > param.getValMin()) {
                return false;
            }

            //obriga a comparar APÓS limite de tolerancia
            if ((headTime.getTime() - measure.getTimestamp().getTime()) / 1000 > tolerance) {
                break;
            }
        }
        return true;
    }

    private Alert zeroToleranceAnalyser(CultureParams param) {
        boolean constantRise = constantRise(param);
        boolean constantFall = constantFall(param);
        Alert alert = null;
        if (constantRise) {
            String msg = "Subida constante perto dos limites definidos, há " + String.valueOf(numCycles) + " medidas consecutivas";
            // alert = ...
        }
        if (constantFall) {
            String msg = "Descida constante perto dos limites definidos, há " + String.valueOf(numCycles) + " medidas consecutivas";
            // alert = ...
        }
        return alert;
    }

    private boolean constantRise(CultureParams param) {
        //sempre a descer, dentro de limites, durante 7 medidas
        double diff = (param.getValMax() - param.getValMin()) * 0.3;
        double minLim = param.getValMax() - diff;

        double val = Double.parseDouble(measurements.get(0).getValue());

        if (val > param.getValMax() || val < minLim || measurements.size() < numCycles) {
            return false;
        }

        for (int ind = 1; ind < numCycles; ind++) {
            double newval = Double.parseDouble(measurements.get(ind).getValue());
            if (newval >= val || newval > param.getValMax() || newval < minLim) {
                return false;
            }
            val = newval;
        }
        return true;
    }

    private boolean constantFall(CultureParams param) {
        //sempre a subir, dentro de limites, durante 7 medidas
        double diff = (param.getValMax() - param.getValMin()) * 0.3;
        double maxLim = param.getValMin() + diff;

        double val = Double.parseDouble(measurements.get(0).getValue());

        if (val > maxLim || val < param.getValMin() || measurements.size() < numCycles) {
            return false;
        }

        for (int ind = 1; ind < numCycles; ind++) {
            double newval = Double.parseDouble(measurements.get(ind).getValue());
            if (newval <= val || newval > maxLim || newval < param.getValMin()) {
                return false;
            }
            val = newval;
        }
        return true;
    }

    private void trimMeasureList() {
        Timestamp maxThreshold = subtractSecondsThreshold(measurements.get(0).getTimestamp(), maxTolerance, insertionRate);
        Iterator<Measurement> it = measurements.iterator();
        while (it.hasNext()) {
            Measurement m = it.next();
            if (m.getTimestamp().compareTo(maxThreshold) < 0) {
                it.remove();
            }
        }
    }

    private Timestamp subtractSecondsThreshold(Timestamp initialTime, int tolerance, int rate) {
        Timestamp newTime = initialTime;
        newTime.setTime(initialTime.getTime() - (tolerance + 3 * rate) * 1000);
        return newTime;
    }

    /*
    public static void main (String[] args) {
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
        p1.setTolerance(0);
        p1.setCulture(c);

        Measurement mea1 = new Measurement();
        mea1.setZone("z1");
        mea1.setSensor("t");
        mea1.setDate("2021-05-02 00:06:30");
        mea1.setMeasure("2.1");

        Measurement mea2 = new Measurement();
        mea2.setZone("z1");
        mea2.setSensor("t");
        mea2.setDate("2021-05-02 00:06:31");
        mea2.setMeasure("2.8");

        Measurement mea3 = new Measurement();
        mea3.setZone("z1");
        mea3.setSensor("t");
        mea3.setDate("2021-05-02 00:06:32");
        mea3.setMeasure("2.7");

        Measurement mea4 = new Measurement();
        mea4.setZone("z1");
        mea4.setSensor("t");
        mea4.setDate("2021-05-02 00:06:33");
        mea4.setMeasure("2.6");

        ArrayList<CultureParams> paramList = new ArrayList<>();
        paramList.add(p1);
//        paramList.add(p2);

        Hashtable<Long, List<CultureParams>> todosParams = new Hashtable<>();
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
