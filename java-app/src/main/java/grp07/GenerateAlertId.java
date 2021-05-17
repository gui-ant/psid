package grp07;

public class GenerateAlertId {

    public static Long constantRiseAlertId (MySqlData.CultureParams param) {
        StringBuilder sb = new StringBuilder();
        sb.append(param.getSensorType());
//        sb.append(String.valueOf(param.getValMax()));
//        sb.append(String.valueOf(param.getValMin()));
//        sb.append(String.valueOf(param.getTolerance()));
        sb.append(String.valueOf(param.getCulture().getId()));
        sb.append(String.valueOf(true));

        Long code = Long.valueOf(sb.toString().hashCode());
        return code;
    }

    public static Long constantFallAlertId (MySqlData.CultureParams param) {
        StringBuilder sb = new StringBuilder();
        sb.append(param.getSensorType());
//        sb.append(String.valueOf(param.getValMax()));
//        sb.append(String.valueOf(param.getValMin()));
//        sb.append(String.valueOf(param.getTolerance()));
        sb.append(String.valueOf(param.getCulture().getId()));
        sb.append(String.valueOf(false));

        Long code = Long.valueOf(sb.toString().hashCode());
        return code;
    }

    public static Long percentageAlertId (String sensor, String zone) {
        Long code = Long.MAX_VALUE;
        code -= sensor.hashCode();
        code -= zone.hashCode();
        return code;
    }
}
