public class Sensor {
    private final int id;
    private Zone zone;
    private double minLim;
    private double maxLim;

    public Sensor(int id) {
        this.id = id;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public int getId() {
        return id;
    }

    public double getMinLim() {
        return minLim;
    }

    public void setMinLim(double minLim) {
        this.minLim = minLim;
    }

    public double getMaxLim() {
        return maxLim;
    }

    public void setMaxLim(double maxLim) {
        this.maxLim = maxLim;
    }

}

