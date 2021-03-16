import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.lang.reflect.Constructor;

public class Measurement {
    private MongoID _id;
    private String Zona;
    private String Sensor;
    private String Data;
    private String Medicao;

    public Measurement(String id, String zona, String sensor, String data, String medicao) {
        MongoID origin_id = new MongoID(id);
        this._id = origin_id;
        Zona = zona;
        Sensor = sensor;
        Data = data;
        Medicao = medicao;
    }

    public String get_id() {
        return _id.getOrigin_id();
    }

    public void set_id(String id) {
        this._id.setOrigin_id(id);
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

    public boolean measequals(Measurement m) {
        if (m == null) return false;
        if (this == m) return true;

        return this.get_id().equals(m.get_id());
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

    class MongoID {

        private String origin_id;

        public MongoID(String id) {
            this.origin_id = id;
        }

        public String getOrigin_id() {
            return origin_id;
        }

        public void setOrigin_id(String origin_id) {
            this.origin_id = origin_id;
        }

        @Override
        public String toString() {
            return this.origin_id;
        }
    }

}


