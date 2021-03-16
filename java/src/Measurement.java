public class Measurement {
    //    private MongoID _id;
    private String _id;
    private final String Zona;
    private final String Sensor;
    private final String Data;
    private final String Medicao;

    public Measurement(String id, String zona, String sensor, String data, String medicao) {
        this._id = id;
        Zona = zona;
        Sensor = sensor;
        Data = data;
        Medicao = medicao;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String id) {
        this._id = id;
    }

    public String getZona() {
        return Zona;
    }

    public String getSensor() {
        return Sensor;
    }

    public String getData() {
        return Data;
    }

    public String getMedicao() {
        return Medicao;
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
}
