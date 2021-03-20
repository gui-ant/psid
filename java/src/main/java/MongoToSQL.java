import org.bson.Document;
import org.bson.types.ObjectId;

import javax.sound.midi.Soundbank;
import java.sql.Connection;
import java.sql.SQLOutput;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;


public class MongoToSQL {
    private final Connection connection;
    private final SqlSender sender;
    private final int sleep_time;

    public MongoToSQL(Connection connection, SqlSender sender, int sleep_time_seconds) {
        this.connection = connection;
        this.sender = sender;
        this.sleep_time = (sleep_time_seconds * 1000);
    }

    public void serveSQL(ConcurrentHashMap<String, LinkedBlockingQueue<Document>> sourceBuffer) {
        sourceBuffer.forEach(
                (collectionName, buffer) -> {
                    new SqlPublisher(connection, buffer, sender, sleep_time).start();
                }
        );
    }

    static class SqlPublisher extends Thread {
        private final LinkedBlockingQueue<Document> buffer;
        private final SqlSender sender;
        private final Connection connection;
        private int sleep_time;

        SqlPublisher(Connection connection, LinkedBlockingQueue<Document> buffer, SqlSender sender, int sleep_time) {
            this.connection = connection;
            this.buffer = buffer;
            this.sender = sender;
            this.sleep_time = sleep_time;
        }

        @Override
        public void run() {
            MeasurementPOJO lastValidMeas = null;

            while (true) {
                try {
                    sleep(sleep_time);

                    emptyBufferRoutine();

                    int counter = 0;
                    double mean_value, acc = 0;

                    Document doc = buffer.poll();

                    //TODO: Trocar esta conversão por atribuição direta
                    MeasurementPOJO measurement = MongoTreatment.convertDocToMeasurement(doc);

                    if (isNotValid(measurement))
                        publish(measurement, false);
                    else {
                        acc += Double.parseDouble(measurement.getMeasure());
                        counter++;
                        lastValidMeas = measurement;
                    }

                    //TODO: tratar o caso da ultima medida ser inválida
                    if (counter != 0) {
                        mean_value = acc / counter;
                        lastValidMeas.setMeasure(Double.toString(mean_value));
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
            if(buffer.isEmpty()){
                int empty_counter = 0;
                sleep(sleep_time);

                while(buffer.isEmpty()){
                    System.err.println("Buffer vazio");
                    if(empty_counter <= 10) {
                        empty_counter++;
                        sleep_time += 1000;
                    }

                    sleep(sleep_time);
                }

                sleep_time -= empty_counter * 1000;
            }
        }


        private boolean isNotValid(MeasurementPOJO measurement) {
            double min = sender.getSensors().get(measurement.getSensor()).getMinLim();
            double max = sender.getSensors().get(measurement.getSensor()).getMaxLim();
            double value = Double.parseDouble(measurement.getMeasure());

            return value < min || value > max;
        }

        private void publish(MeasurementPOJO measurement, boolean isValid){
            sender.send(connection, measurement, isValid);
        }
    }
}
