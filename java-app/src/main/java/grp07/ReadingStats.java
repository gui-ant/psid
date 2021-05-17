package grp07;

public class ReadingStats {
    private int totalReadings;
    private int totalErrors;
    private String sensor;
    private String zone;

    public ReadingStats () {
        this.totalReadings = 0;
        this.totalErrors = 0;
        this.sensor = "";
        this.zone = "";
    }

    public int getTotalReadings() {
        return totalReadings;
    }

    public int getTotalErrors() {
        return totalErrors;
    }

    public synchronized void incrementReadings() {
        totalReadings++;
    }

    public synchronized void incrementErrors() {
        totalErrors++;
    }

    public synchronized void resetData() {
        totalReadings = 0;
        totalErrors = 0;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public String getSensor() {
        return sensor;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getZone() {
        return zone;
    }
}
