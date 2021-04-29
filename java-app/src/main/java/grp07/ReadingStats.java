package grp07;

public class ReadingStats {
    private int totalReadings;
    private int totalErrors;

    public ReadingStats () {
        this.totalReadings = 0;
        this.totalErrors = 0;
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
}
