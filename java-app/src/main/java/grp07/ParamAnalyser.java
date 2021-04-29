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
            if (isSuspect(param)) {
                preAlertCompiler.addPreAlert(Timestamp.from(Instant.now()), param);
            }
            if (param.getTolerance() > maxTolerance) {
                maxTolerance = param.getTolerance();
            }
        }
        trimMeasureList();
    }

    private boolean isSuspect (CultureParams param) {
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
