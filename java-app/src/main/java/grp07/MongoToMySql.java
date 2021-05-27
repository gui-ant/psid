package grp07;

import common.IniConfig;
import common.MeasurementMySqlPublisher;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;


public class MongoToMySql extends IniConfig {
    private static final String MYSQL_USER = "root";
    private static final String MYSQL_PASS = "";

    private final Connection mysqlConn;
    private MySqlData data;
    private MySqlData.Sensor sensor;

    private final long sleepTime;
    // PreAlertSet (comum a threads e supervisor)
    private final PreAlertSet preAlertSet;


    public MongoToMySql(Connection mysqlConn, MySqlData data, long sleepTime) {
        super("config.ini");
        this.mysqlConn = mysqlConn;
        this.data = data;
        this.sleepTime = sleepTime;
        this.sensor = null;

        // criar PreAlertSet e Supervisor
        this.preAlertSet = new PreAlertSet(data.getCultureParamsSet());
        ParameterSupervisor supervisor = new ParameterSupervisor(preAlertSet);
        supervisor.start();

    }

    public void serveSQL(HashMap<String, LinkedBlockingQueue<Measurement>> sourceBuffer) {
        sourceBuffer.forEach(
                (collectionName, buffer) -> new SqlPublisher(buffer, sleepTime, preAlertSet).start()
        );
    }

    public class SqlPublisher extends MeasurementMySqlPublisher {

        private final LinkedBlockingQueue<Measurement> buffer;
        private final double ERROR_PERCENTAGE = 0.33;
        private long sleepTime;
        //analyser individual (de cada thread)
        private final PreAlertSet preAlertSet;
        private ParamAnalyser analyser;

        private final ReadingStats stats;

        public SqlPublisher(LinkedBlockingQueue<Measurement> buffer, long sleepTime, PreAlertSet preAlertSet) {
            super(mysqlConn, data, buffer);
            this.buffer = buffer;
            this.sleepTime = sleepTime;

            //this.analyser = new ParamAnalyser(preAlertSet, null, sleepTime);
            this.preAlertSet = preAlertSet;
            this.analyser = createAnalyser(data, sleepTime);

            this.stats = new ReadingStats();
            ErrorSupervisor es = new ErrorSupervisor(stats, ERROR_PERCENTAGE);
            es.start();

            SqlDbUpdater updater = new SqlDbUpdater();
            updater.start();
        }

        @Override
        public void run() {

            Measurement lastValidMeas = null;

            while (true) {
                if (analyser == null) {
                    analyser = createAnalyser(data, sleepTime);
                }
                try {
                    sleep(sleepTime);

                    emptyBufferRoutine();

                    int counter = 0;
                    double meanValue, acc = 0;


                    while(!buffer.isEmpty()){

                        Measurement measurement = buffer.take();
                        stats.incrementReadings();
                        if (sensor == null) {
                            sensor = data.getSensorByName(measurement.getSensor());
                        }

                        if (!isValid(measurement)) {
                            publish(measurement);
                            stats.incrementErrors();
                        } else {
                            acc += measurement.getRoundValue();
                            counter++;
                            lastValidMeas = measurement;
                        }
                        // Confrontar a medição com as parametrizações que existem
                        // para a tipologia de sensor dessa medição (H, T, L)
                        //
                        // tipologia de sensor: measurement.getSensorType();


                    }

                    if (counter != 0) {
                        meanValue = acc / counter;
                        lastValidMeas.setValue(String.valueOf(meanValue));

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
                int emptyCounter = 0;
                sleep(sleepTime);

                while (buffer.isEmpty()) {
                    System.err.println("Buffer vazio");
                    if (emptyCounter <= 10) {
                        emptyCounter++;
                        sleepTime += 1000;
                    }

                    sleep(sleepTime);
                }

                sleepTime -= emptyCounter * 1000L;
            }
        }

        /*private void publish(Measurement measurement, boolean isValid) {
            //sender.send(connection, measurement, isValid);
        }*/

        // cria um analisador de parametros, com os parametros filtrados desta thread
        private ParamAnalyser createAnalyser(MySqlData data, long rate) {
            ArrayList<MySqlData.CultureParams> list = new ArrayList<>();
            Measurement mea = buffer.peek();
            if (mea == null) {
                return null;
            }

            Hashtable<Long, List<MySqlData.CultureParams>> cultureParamsSet = data.getCultureParamsSet();
            MySqlData.Zone zone = data.getZoneByName(mea.getZone());

            for (List<MySqlData.CultureParams> params : cultureParamsSet.values()) {
                for (MySqlData.CultureParams param : params) {
                    if (param.getSensorType().equals(mea.getSensorType()) && param.getCulture().getZone().equals(zone)) {
                        list.add(param);
                    }
                }
            }

            return new ParamAnalyser(preAlertSet, list, rate);

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
                    double totalReadings = stats.getTotalReadings();
                    double totalErrors = stats.getTotalErrors();
                    if (totalReadings != 0) {
                        if (totalErrors / totalReadings >= percentage) {

                            StringBuilder sb = new StringBuilder();
                            sb.append("Atencao, ao sensor " + sensor.getId() + " da zona " + sensor.getZone().getId() + "!");
                            sb.append(" O sensor apresenta uma percentagem de erros superior a " + percentage * 100 + "%.");

                            Alert alert = new Alert(0, 0, sensor.getId(), 0, Timestamp.from(Instant.now()), sb.toString());

                            try {
                                Connection mysql = DriverManager.getConnection(getConfig("mysql", "local_uri"), MYSQL_USER, MYSQL_PASS);
                                String sql = "INSERT INTO alerts (sensor_id, created_at, message) VALUES (?, ?, ?)";

                                PreparedStatement statement = mysql.prepareStatement(sql);
                                statement.setLong(1, alert.getSensorId());
                                statement.setTimestamp(2, alert.getCreatedAt());
                                statement.setString(3, alert.getMsg());
                                statement.execute();

                            } catch (SQLException throwables) {
                                System.err.println("Alerta rejeitado. Já existe alerta anterior para a mesma parametrização nos últimos 15 min.");
                            }
                        }
                    }
                    stats.resetData();
                }
            }
        }


        public class SqlDbUpdater extends Thread {

            public SqlDbUpdater() {
            }

            public void run() {
                while (true) {
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // TODO - testar esta merda!!!
                    data = new MySqlData("config.ini");
                    synchronized (this) {
                        analyser = createAnalyser(data, sleepTime);
                        preAlertSet.setAllParams(data.getCultureParamsSet());
                    }
                }
            }
        }
    }
}
