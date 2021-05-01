package grp07;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

// CADA THREAD TEM UM PARAMANALYSER!!!
public class ParamAnalyser {
    private PreAlertSet preAlertCompiler;
    private List<CultureParams> paramList;  //lista de parametros INDIVIDUAIS de um dado sensor
    private List<Measurement> measurements;
    private int maxTolerance;
    private int insertionRate;  //frequencia a que as medidas são geradas

    public ParamAnalyser (PreAlertSet preAlertCompiler, ArrayList<CultureParams> paramList, int insertionRate) {
        this.preAlertCompiler = preAlertCompiler;
        this.paramList = paramList;
        this.measurements = new LinkedList<>();
        this.maxTolerance = 0;
        this.insertionRate = insertionRate;
    }

    public void addMeasurement (Measurement measure) {
        this.measurements.add(0, measure);
    }

    public void setParamList (ArrayList<CultureParams> paramList) {
        this.paramList = paramList;
    }

    public void analyseParameters() {
        for (CultureParams param : paramList) {
            boolean isSus = isSuspect(param);
            if (isSus) {
                preAlertCompiler.addPreAlert(Timestamp.from(Instant.now()), param, true);
            }
            if (! isSus) {
                preAlertCompiler.addPreAlert(Timestamp.from(Instant.now()), param, false);
            }
            if (param.getTolerance() > maxTolerance) {
                maxTolerance = param.getTolerance();
            }
        }
        trimMeasureList();
    }

    private boolean isSuspect (CultureParams param) {

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
            if (Double.parseDouble(measure.getMeasure()) < param.getValMax() || Double.parseDouble(measure.getMeasure()) > param.getValMin()) {
                return false;
            }

            //obriga a comparar APÓS limite de tolerancia
            if (headTime.compareTo(measure.getTimestamp()) > tolerance) {
                break;
            }
        }
        return true;
    }

    private Alert zeroToleranceAnalyser(CultureParams param) {
        boolean constantRise = constantRise(param);
        boolean constantFall = constantFall(param);
        Alert alert = null;
        if(constantRise) {
            // alert = ...
        }
        if(constantFall) {
            // alert = ...
        }
        return alert;
    }

    private boolean constantRise (CultureParams param) {
        //sempre a descer, dentro de limites, durante 7 medidas
        double diff = (param.getValMax() - param.getValMin()) * 0.3;
        double minLim = param.getValMax() - diff;
        int numCycles = 7;
        double val = Double.parseDouble(measurements.get(0).getMeasure());

        if (val > param.getValMax() || val < minLim || measurements.size() < numCycles) {
            return false;
        }

        for (int ind = 1; ind < numCycles; ind++) {
            double newval = Double.parseDouble(measurements.get(ind).getMeasure());
            if (newval >= val || newval > param.getValMax() || newval < minLim) {
                return false;
            }
            val = newval;
        }
        return true;
    }

    private boolean constantFall (CultureParams param) {
        //sempre a subir, dentro de limites, durante 7 medidas
        double diff = (param.getValMax() - param.getValMin()) * 0.3;
        double maxLim = param.getValMin() + diff;
        int numCycles = 7;
        double val = Double.parseDouble(measurements.get(0).getMeasure());

        if (val > maxLim || val < param.getValMin() || measurements.size() < numCycles) {
            return false;
        }

        for (int ind = 1; ind < numCycles; ind++) {
            double newval = Double.parseDouble(measurements.get(ind).getMeasure());
            if (newval <= val || newval > maxLim || newval < param.getValMin()) {
                return false;
            }
            val = newval;
        }
        return true;
    }

    private void trimMeasureList() {
        List<Measurement> trimmedMeasures = new LinkedList<>();
        Timestamp headTime = measurements.get(0).getTimestamp();
        Timestamp maxThreshold = addSecondsThreshold(headTime, maxTolerance, insertionRate);

        for (Measurement measure : measurements) {
            if (measure.getTimestamp().before(maxThreshold)) {
                trimmedMeasures.add(measure);
            }
        }
        this.measurements = trimmedMeasures;
    }

    private Timestamp addSecondsThreshold(Timestamp initialTime, int tolerance, int rate) {
        //ver com iterator???

        Timestamp newTime = initialTime;
        newTime.setTime(initialTime.getTime() + (tolerance + 3*rate)*1000);
        return newTime;
    }
}
