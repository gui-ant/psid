package grp07;

import common.MeasurementMySqlPublisher;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;


public class MongoToMySql {

    private final Connection mysqlConn;
    private final MySqlData data;

    private final int sleep_time;
    // PreAlertSet (comum a threads e supervisor)
    private final PreAlertSet preAlertSet;
    private final ParameterSupervisor supervisor;


    public MongoToMySql(Connection mysqlConn, MySqlData data, int sleep_time_seconds) {
        this.mysqlConn = mysqlConn;
        this.data = data;
        this.sleep_time = (sleep_time_seconds * 1000);

        // criar PreAlertSet e Supervisor
        this.preAlertSet = new PreAlertSet(data.getCultureParamsSet());
        this.supervisor = new ParameterSupervisor(preAlertSet);
        supervisor.start();
    }

    public void serveSQL(ConcurrentHashMap<String, LinkedBlockingQueue<Measurement>> sourceBuffer) {
        sourceBuffer.forEach(
                (collectionName, buffer) -> new SqlPublisher(buffer, sleep_time, preAlertSet).start()
        );
    }

    public class SqlPublisher extends MeasurementMySqlPublisher {

        private final LinkedBlockingQueue<Measurement> buffer;
        private final double ERROR_PERCENTAGE = 0.33;
        private int sleep_time;
        //analyser individual (de cada thread)
        private final PreAlertSet preAlertSet;
        private ParamAnalyser analyser;

        private final ReadingStats stats;

        public SqlPublisher(LinkedBlockingQueue<Measurement> buffer, int sleep_time, PreAlertSet preAlertSet) {
            super(mysqlConn, buffer);
            this.buffer = buffer;
            this.sleep_time = sleep_time;

            //this.analyser = new ParamAnalyser(preAlertSet, null, sleep_time);
            this.preAlertSet = preAlertSet;
            this.analyser = createAnalyser(data, sleep_time);

            this.stats = new ReadingStats();
            ErrorSupervisor es = new ErrorSupervisor(stats, ERROR_PERCENTAGE);
            es.start();
        }

        @Override
        public void run() {

            Measurement lastValidMeas = null;

            while (true) {
                if (analyser == null) {
                    analyser = createAnalyser(data, sleep_time);
                }
                try {
                    sleep(sleep_time);

                    emptyBufferRoutine();

                    int counter = 0;
                    double mean_value, acc = 0;

                    Measurement measurement = buffer.take();
                    stats.incrementReadings();

                    if (!isValid(measurement)) {
                        publish(measurement);
                        stats.incrementErrors();
                    } else {
                        acc += Double.parseDouble(measurement.getValue());
                        counter++;
                        lastValidMeas = measurement;
                    }
                    // Confrontar a medição com as parametrizações que existem
                    // para a tipologia de sensor dessa medição (H, T, L)
                    //
                    // tipologia de sensor: measurement.getSensorType();

                    if (counter != 0) {
                        mean_value = acc / counter;
                        lastValidMeas.setValue(Double.toString(mean_value));

                        // TODO - É AQUI???
                        if (analyser != null) {
                            analyser.addMeasurement(lastValidMeas);
                            analyser.analyseParameters();
                        }

                        publish(lastValidMeas);
                    }


                } catch (InterruptedException | SQLException e) {
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

        /*private void publish(Measurement measurement, boolean isValid) {
            //sender.send(connection, measurement, isValid);
        }*/

        // cria um analisador de parametros, com os parametros filtrados desta thread
        // TODO - testar!!!
        private ParamAnalyser createAnalyser(MySqlData data, int rate) {
            ArrayList<MySqlData.CultureParams> list = new ArrayList<>();
            Measurement mea = buffer.peek();
            if (mea == null) {
                return null;
            }

            Hashtable<Long, MySqlData.CultureParams> cultureParamsSet = data.getCultureParamsSet();
            MySqlData.Zone zone = data.getZones().get(Long.parseLong(String.valueOf(mea.getZone().charAt(1))));

            cultureParamsSet.forEach((id, param) -> {
                if (param.getSensorType().equals(mea.getSensorType()) && param.getCulture().getZone().equals(zone)) {
                    list.add(param);
                }
            });

            ParamAnalyser an = new ParamAnalyser(preAlertSet, list, rate);
            return an;
        }

        /**
         * Ignorar esté método para já
         */
        @Override
        protected synchronized void handle(Measurement measurement) {
        }

        //thread que analisa a percentagem de leituras errada, a cada hora
        private class ErrorSupervisor extends Thread {
            private final ReadingStats stats;
            private final double percentage;

            public ErrorSupervisor(ReadingStats stats, double percentage) {
                this.stats = stats;
                this.percentage = percentage;
                System.out.println("ErrorSupervisor ligado!!!");
            }

            public void run() {
                while (true) {
                    try {
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
