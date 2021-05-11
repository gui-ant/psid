package grp07;

import common.SqlConnector;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;


public class MongoToSQL {
    private final SqlConnector connection; //local
    private final SqlDataHandler sqlDataHandler;
    private final int sleep_time;
    // PreAlertSet (comum a threads e supervisor)
    private PreAlertSet preAlertSet;
    private ParameterSupervisor supervisor;

    public MongoToSQL(SqlConnector connection, SqlDataHandler sqlDataHandler, int sleep_time_seconds) {
        this.connection = connection;
        this.sqlDataHandler = sqlDataHandler;
        this.sleep_time = (sleep_time_seconds * 1000);

        // criar PreAlertSet e Supervisor
        this.preAlertSet = new PreAlertSet(sqlDataHandler.getCultureParamsSet());
        this.supervisor = new ParameterSupervisor(preAlertSet);
        supervisor.start();
    }

    public void serveSQL(ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> sourceBuffer) {
        sourceBuffer.forEach(
                (collectionName, buffer) -> {
                    new AlertsPublisher(connection, buffer, sqlDataHandler, sleep_time, preAlertSet).start();
                }
        );
    }

    static class AlertsPublisher extends Thread {
        private final SqlDataHandler sender;
        private final SqlConnector connection;
        private final LinkedBlockingQueue<Measurement> buffer;
        private final double ERROR_PERCENTAGE = 0.33;
        private int sleep_time;
        //analyser individual (de cada thread)
        private PreAlertSet preAlertSet;
        private ParamAnalyser analyser;

        private ReadingStats stats;

        AlertsPublisher(SqlConnector connection, LinkedBlockingQueue<Measurement> buffer, SqlDataHandler sender, int sleep_time, PreAlertSet preAlertSet) {
            this.buffer = buffer;
            this.connection = connection;
            this.sender = sender;
            this.sleep_time = sleep_time;

            //this.analyser = new ParamAnalyser(preAlertSet, null, sleep_time);
            this.preAlertSet = preAlertSet;
            this.analyser = createAnalyser(sender, sleep_time);

            stats = new ReadingStats();
            ErrorSupervisor es = new ErrorSupervisor(stats, ERROR_PERCENTAGE);
            es.start();
        }

        @Override
        public void run() {

            Measurement lastValidMeas = null;

            while (true) {
                if (analyser == null) {
                    analyser = createAnalyser(sender, sleep_time);
                }
                try {
                    sleep(sleep_time);

                    emptyBufferRoutine();

                    int counter = 0;
                    double mean_value, acc = 0;

                    Measurement measurement = buffer.poll();
                    stats.incrementReadings();

                    if (isNotValid(measurement)) {
                        publish(measurement, false);
                        stats.incrementErrors();
                    }
                    else {
                        acc += Double.parseDouble(measurement.getValue());
                        counter++;
                        lastValidMeas = measurement;

                       // É AQUI???
                       analyser.addMeasurement(measurement);
                       analyser.analyseParameters();
                    }
                    // Confrontar a medição com as parametrizações que existem
                    // para a tipologia de sensor dessa medição (H, T, L)
                    //
                    // tipologia de sensor: measurement.getSensorType();

                    if (counter != 0) {
                        mean_value = acc / counter;
                        lastValidMeas.setValue(Double.toString(mean_value));
                        publish(lastValidMeas, true);
                    }


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }


        // Por cada vez que vai ao buffer e este está vazio, aumenta o tempo de espera
        // por 1s até um máx de 10s. Quando volta a ter documento, dá reset ao tempo
        // para o valor inserido pelo utilizador.
        private void emptyBufferRoutine() throws InterruptedException {
            if (buffer.isEmpty()) {
                int empty_counter = 0;
                sleep(sleep_time);

                while (buffer.isEmpty()) {
                    System.err.println("Buffer vazio");
                    if (empty_counter <= 10) {
                        empty_counter++;
                        sleep_time += 1000;
                    }

                    sleep(sleep_time);
                }

                sleep_time -= empty_counter * 1000;
            }
        }


        private boolean isNotValid(Measurement measurement) {
            double min = sender.getSensors().get(measurement.getSensor()).getMinLim();
            double max = sender.getSensors().get(measurement.getSensor()).getMaxLim();
            double value = Double.parseDouble(measurement.getValue());

            return value < min || value > max;
        }

        private void publish(Measurement measurement, boolean isValid) {
            //sender.send(connection, measurement, isValid);
        }

        private ParamAnalyser createAnalyser(SqlDataHandler sender, int rate) {
            ArrayList<CultureParams> list = new ArrayList<>();
            Measurement mea = buffer.peek();
            if (mea == null) {
                return null;
            }
            Hashtable<Long, List<CultureParams>> cultureParamsSet = sender.getCultureParamsSet();
            Zone zone = sender.getZones().get(mea.getZone());

            for(List<CultureParams> params : cultureParamsSet.values()) {
                for(CultureParams param : params) {
                    if(param.getSensorType().equals(mea.getSensorType()) && param.getCulture().getZone().equals(zone)) {
                        list.add(param);
                    }
                }
            }
            ParamAnalyser an = new ParamAnalyser(preAlertSet, list, rate);
            return an;
        }


        private class ErrorSupervisor extends Thread {
            private ReadingStats stats;
            private double percentage;

            public ErrorSupervisor (ReadingStats stats, double percentage) {
                this.stats = stats;
                this.percentage = percentage;
                System.out.println("ErrorSupervisor ligado!!!");
            }

            public void run() {
                while (true) {
                    try {
                        System.out.println("ErrorSupersivor: antes do sleep");
                        System.out.println(stats.getTotalReadings());
                        sleep(3600 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int totalReadings = stats.getTotalReadings();
                    int totalErrors = stats.getTotalErrors();
                    if (totalErrors/totalReadings >= percentage && totalReadings != 0) {
                        //ENVIAR ALERTA!!!
                    }
                    stats.resetData();
                }
            }
        }
    }
}
