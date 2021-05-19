package common;

public enum MigrationMethod {
    DIRECT,
    MQTT;

    public static MigrationMethod getByValue(String value) {
        return MigrationMethod.getByValue(Integer.valueOf(value));
    }

    public static MigrationMethod getByValue(int value) {
        return MigrationMethod.values()[value];
    }
}
