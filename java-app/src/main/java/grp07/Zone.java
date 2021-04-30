package grp07;

public class Zone {
    private int id;
    private double temperature;
    private double humidity;
    private double light;

    public Zone(int id) {
        this.id = id;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getLight() {
        return light;
    }

    public void setLight(double light) {
        this.light = light;
    }

    public int getId() {
        return this.id;
    }

    public boolean isEqual(Zone z) {
        if (id == z.getId() && temperature == z.getTemperature() && humidity == z.getHumidity() && light == z.getLight()) {
            return true;
        }
        else {
            return false;
        }
    }

}
