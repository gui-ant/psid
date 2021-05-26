package grp07;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.sql.Timestamp;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * The Measurement Pojo
 */
public final class Measurement {

    @JsonProperty(value = "_id")
    private ObjectId id;
    @JsonProperty(value = "Zona")
    @BsonProperty(value = "Zona")
    private String zone;
    @JsonProperty(value = "Sensor")
    @BsonProperty(value = "Sensor")
    private String sensor;
    @JsonProperty(value = "Data")
    @BsonProperty(value = "Data")
    private String date;
    @JsonProperty(value = "Medicao")
    @BsonProperty(value = "Medicao")
    private String value;

    public Measurement() {
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
        return date.replaceAll("[TZ]", " ").trim();
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = String.valueOf((double) Math.round(Double.parseDouble(value) * 100) / 100);
    }

    @BsonIgnore
    @JsonIgnore
    public String getSensorType() {
        return sensor.substring(0, 1);
    }

    @BsonIgnore
    @JsonIgnore
    public Timestamp getTimestamp() {
        return java.sql.Timestamp.valueOf(getDate());
    }

    @BsonIgnore
    @JsonIgnore
    public byte[] toByteArray() {
        return this.toString().getBytes(UTF_8);
    }

    @BsonIgnore
    @JsonIgnore
    public Double getRoundValue() {
        return Double.parseDouble(this.value);
    }
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Measurement m = (Measurement) o;

        return getId() == m.getId();
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getZone() != null ? getZone().hashCode() : 0);
        result = 31 * result + (getSensor() != null ? getSensor().hashCode() : 0);
        result = 31 * result + (getDate() != null ? getDate().hashCode() : 0);
        result = 31 * result + (getValue() != null ? getValue().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "{" +
                "_id:\"" + this.id + "\"" +
                ",Zona:\"" + this.zone + "\"" +
                ",Sensor:\"" + this.sensor + "\"" +
                ",Data:\"" + this.date + "\"" +
                ",Medicao:\"" + this.value + "\"" +
                "}";
    }
}