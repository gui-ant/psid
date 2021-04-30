package grp07;

public class CultureParams {
    private String sensorType;
    private double valMax;
    private double valMin;
    private int tolerance;
    private Culture culture;

    public Culture getCulture() {
        return culture;
    }

    public void setCulture(Culture culture) {
        this.culture = culture;
    }

    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public double getValMax() {
        return valMax;
    }

    public void setValMax(double valMax) {
        this.valMax = valMax;
    }

    public double getValMin() {
        return valMin;
    }

    public void setValMin(double valMin) {
        this.valMin = valMin;
    }

    public int getTolerance() {
        return tolerance;
    }

    public void setTolerance(int tolerance) {
        this.tolerance = tolerance;
    }

    public boolean isEqual(CultureParams param) {
        if (sensorType.equals(param.getSensorType()) && valMax == param.getValMax() && valMin == param.getValMin() && tolerance == param.getTolerance() && culture.equals(param.getCulture())) {
            return true;
        }
        else {
            return false;
        }
    }
}
