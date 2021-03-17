import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.sql.Timestamp;

/**
 * The Measurement Pojo
 */
public final class MeasurementPOJO {

    private ObjectId id;
    @BsonProperty(value = "Zona")
    private String zone;
    @BsonProperty(value = "Sensor")
    private String sensor;

    @BsonProperty(value = "Data")
    private String date;
    @BsonProperty(value = "Medicao")
    private String measure;

    public MeasurementPOJO() {
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(final ObjectId id) {
        this.id = id;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public String getDate() {
        return date.replaceAll("(T|Z)", " ").trim();
    }

    public Timestamp getTimestamp() {
        return java.sql.Timestamp.valueOf(getDate());
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MeasurementPOJO m = (MeasurementPOJO) o;

        return getId() == m.getId();
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getZone() != null ? getZone().hashCode() : 0);
        result = 31 * result + (getSensor() != null ? getSensor().hashCode() : 0);
        result = 31 * result + (getDate() != null ? getDate().hashCode() : 0);
        result = 31 * result + (getMeasure() != null ? getMeasure().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Medicao{" +
                "_id=" + this.id +
                ", Zona='" + this.zone + "'" +
                ", Sensor='" + this.sensor + "'" +
                ", Data='" + this.date + "'" +
                ", Medicao='" + this.measure + "'" +
                '}';
    }
}