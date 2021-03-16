import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

/**
 * The Measurement Pojo
 */
public final class MeasurementPOJO {

    private ObjectId id;
    @BsonProperty(value = "Zona")
    private String zone;
    @BsonProperty(value = "Tipo")
    private String type;
    @BsonProperty(value = "Data")
    private String date;
    @BsonProperty(value = "Medida")
    private String value;

    public MeasurementPOJO() {
    }

    public MeasurementPOJO(final String zone, final String type, final String date, final String value) {
        this.zone = zone;
        this.type = type;
        this.date = date;
        this.value = value;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /* TODO: Implementar
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Person person = (Person) o;

        if (getAge() != person.getAge()) {
            return false;
        }
        if (getId() != null ? !getId().equals(person.getId()) : person.getId() != null) {
            return false;
        }
        if (getName() != null ? !getName().equals(person.getName()) : person.getName() != null) {
            return false;
        }
        if (getAddress() != null ? !getAddress().equals(person.getAddress()) : person.getAddress() != null) {
            return false;
        }

        return true;
    }*/

    /* TODO: Implementar
    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + getAge();
        result = 31 * result + (getAddress() != null ? getAddress().hashCode() : 0);
        return result;
    }*/

    /* TODO: Implementar
    @Override
    public String toString() {
        return "Person{"
                + "id='" + id + "'"
                + ", name='" + name + "'"
                + ", age=" + age
                + ", address=" + address
                + "}";
    }*/
}