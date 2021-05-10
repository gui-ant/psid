package grp07;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;


public class MongoToSQL {
    private final Connection connection; //local
    private final SqlSender sender;
    private final int sleep_time;
    // PreAlertSet (comum a threads e supervisor)
    private PreAlertSet preAlertSet;
    private ParameterSupervisor supervisor;

    public MongoToSQL(Connection connection, SqlSender sender, int sleep_time_seconds) {
        this.connection = connection;
        this.sender = sender;
        this.sleep_time = (sleep_time_seconds * 1000);

        // criar PreAlertSet e Supervisor
        this.preAlertSet = new PreAlertSet(sender.getCultureParamsSet());
        this.supervisor = new ParameterSupervisor(preAlertSet);
        supervisor.start();
    }

    public List<Sensor> getSensorsInfo() {
        List<Sensor> s = new ArrayList<>();
        List<Zone> z = new ArrayList<>();
        return s;
    }

    public void serveSQL(ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> sourceBuffer) {
        sourceBuffer.forEach(
                (collectionName, buffer) -> {
                    new SqlPublisher(connection, buffer, sender, sleep_time, preAlertSet).start();
                }
        );
    }

    static class SqlPublisher extends Thread {
        private final SqlSender sender;
        private final Connection connection;
        private final LinkedBlockingQueue<Measurement> buffer;
        private final double ERROR_PERCENTAGE = 0.33;
        private int sleep_time;
        //analyser individual (de cada thread)
        private PreAlertSet preAlertSet;
        private ParamAnalyser analyser;

        private ReadingStats stats;

        SqlPublisher(Connection connection, LinkedBlockingQueue<Measurement> buffer, SqlSender sender, int sleep_time, PreAlertSet preAlertSet) {
            this.buffer = buffer;
            this.connection = connection;
            this.sender = sender;
            this.sleep_time = sleep_time;

            //this.analyser = new ParamAnalyser(preAlertSet, null, sleep_time);
            this.preAlertSet = preAlertSet;
            this.analyser = createAnalyser(sender, sleep_time);

            this.stats = new ReadingStats();
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
                        acc += Double.parseDouble(measurement.getMeasure());
                        counter++;
                        lastValidMeas = measurement;

                    }
                    // Confrontar a medição com as parametrizações que existem
                    // para a tipologia de sensor dessa medição (H, T, L)
                    //
                    // tipologia de sensor: measurement.getSensorType();

                    if (counter != 0) {
                        mean_value = acc / counter;
                        lastValidMeas.setMeasure(Double.toString(mean_value));

                        // TODO - É AQUI???
                        analyser.addMeasurement(lastValidMeas);
                        analyser.analyseParameters();

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
            double value = Double.parseDouble(measurement.getMeasure());

            return value < min || value > max;
        }

        private void publish(Measurement measurement, boolean isValid) {
            sender.send(connection, measurement, isValid);
        }

        // cria um analisador de parametros, com os parametros filtrados desta thread
        // TODO - testar!!!
        private ParamAnalyser createAnalyser(SqlSender sender, int rate) {
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

        //thread que analisa a percentagem de leituras errada, a cada hora
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
                    if (totalReadings != 0) {
                        if (totalErrors / totalReadings >= percentage) {
                            // TODO - ENVIAR ALERTA!!!
                        }
                    }
                    stats.resetData();
                }
            }
        }
    }
}
