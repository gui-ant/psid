import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

public class Measurement {
    private ID _id;
    //@BsonProperty(value = "sensor_id")
    private String Zona;
    //@BsonProperty(value = "zone_id")
    private String Sensor;
    private String Data;
    private String Medicao;

    public Measurement(ID _id, String zona, String sensor, String data, String medicao) {
        this._id = _id;
        Zona = zona;
        Sensor = sensor;
        Data = data;
        Medicao = medicao;
    }

    public Measurement(){}

    public ID get_id() {
        return _id;
    }

    public void set_id(ID _id) {
        this._id = _id;
    }

    public String getZona() {
        return Zona;
    }

    public void setZona(String zona) {
        Zona = zona;
    }

    public String getSensor() {
        return Sensor;
    }

    public void setSensor(String sensor) {
        Sensor = sensor;
    }

    public String getData() {
        return Data;
    }

    public void setData(String data) {
        Data = data;
    }

    public String getMedicao() {
        return Medicao;
    }

    public void setMedicao(String medicao) {
        Medicao = medicao;
    }

    @Override
    public String toString() {
        return "Medicao{" +
                "_id=" + _id +
                ", Zona='" + Zona + '\'' +
                ", Sensor='" + Sensor + '\'' +
                ", Data='" + Data + '\'' +
                ", Medicao='" + Medicao + '\'' +
                '}';
    }
}


class ID{
    public String oid;
        //Getter setter
}
