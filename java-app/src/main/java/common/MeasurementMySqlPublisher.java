package common;

import grp07.Measurement;
import grp07.MySqlData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class MeasurementMySqlPublisher extends MySqlPublisher<Measurement> {
    public MeasurementMySqlPublisher(Connection mysql_local, MySqlData data, LinkedBlockingQueue<Measurement> buffer) {
        super(mysql_local, data, buffer);
    }

    public void publish(Measurement m) throws SQLException {
        PreparedStatement p = getStatement(m);

        int rows = p.executeUpdate();
        if (rows > 0) {
            System.out.println("Inserted (MySQL):\t" + m);
        }
        p.close();
    }

    @Override
    protected PreparedStatement getStatement(Measurement measurement) {
        // buscar dados e extrair valores

        try {
            String id = measurement.getId().toString();
            MySqlData.Zone zone = getData().getZoneByName(measurement.getZone());
            MySqlData.Sensor sensor = getData().getSensorByName(measurement.getSensor());
            Double value = measurement.getRoundValue();
            //Timestamp date = measurement.getTimestamp();
            Timestamp date = new Timestamp(System.currentTimeMillis());


            //enviar para SQL
            String sql = "INSERT INTO measurements (id, value, sensor_id, zone_id, date, is_correct) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setString(1, id);
            statement.setDouble(2, value);
            statement.setInt(3, sensor.getId());
            statement.setInt(4, zone.getId());
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

        MySqlData.Sensor sensor = getData().getSensorByName(measurement.getSensor());

        double min = sensor.getMinLim();
        double max = sensor.getMaxLim();
        double value = measurement.getRoundValue();
        System.err.println("Parâmetros Sensor -> minLin: " + min + ", maxLim: " + max + ", valid: " + (value > min && value < max));

        return value > min && value < max;
    }

    ;
}