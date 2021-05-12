package common;

import grp07.Measurement;
import grp07.MySqlData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class MeasurementMySqlPublisher extends MySqlPublisher<Measurement> {
    public MeasurementMySqlPublisher(Connection mysql_local, LinkedBlockingQueue<Measurement> buffer) {
        super(mysql_local, MySqlData.get(), buffer);
    }

    public void publish(Measurement m) throws SQLException {
        PreparedStatement p = getStatement(m);

        int rows = p.executeUpdate();
        if (rows > 0) {
            System.out.println("Inserted value successfully!!!");
        }

        p.close();
    }

    @Override
    protected PreparedStatement getStatement(Measurement measurement) {
        // buscar dados e extrair valores

        System.out.println("To insert: " + measurement);
        try {
            String id = measurement.getId().toString();
            MySqlData.Zone zone = getData().getZones().get(measurement.getZoneId());
            MySqlData.Sensor sensor = getData().getSensors().get(measurement.getSensorId());
            String value = measurement.getValue();
            //Timestamp date = measurement.getTimestamp();
            Timestamp date = new Timestamp(System.currentTimeMillis());


            //enviar para SQL
            String sql = "INSERT INTO measurements (id, value, sensor_id, zone_id, date, is_correct) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setString(1, id);
            statement.setString(2, value);
            statement.setInt(3, zone.getId());
            statement.setInt(4, sensor.getId());
            statement.setTimestamp(5, date);
            statement.setBoolean(6, isValid(measurement));


            return statement;
        } catch (Exception e) {
            System.out.println("Connection failed!!!");
            e.printStackTrace();
        }
        return null;
    }

    protected boolean isValid(Measurement measurement) {

        MySqlData.Sensor sensor = getData().getSensors().get(measurement.getSensorId());

        double min = sensor.getMinLim();
        double max = sensor.getMaxLim();
        double value = Double.parseDouble(measurement.getValue());
        System.out.println(measurement);
        System.err.println("Parâmetros Sensor -> minLin: " + min + ", maxLim: " + max + ", valid: " + (value > min && value < max));

        return value > min && value < max;
    }

    ;
}