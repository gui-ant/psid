package com.example.pisid2021.APP.Database;

import android.provider.BaseColumns;

public class DatabaseConfig {

    public static class Measurements implements BaseColumns {
        public static final String TABLE_NAME = "measurements";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_VALUE = "value";
        public static final String COLUMN_SENSOR_ID = "sensor_id";
        public static final String COLUMN_ZONE_ID = "zone_id";
        public static final String COLUMN_IS_CORRECT = "is_correct";
    }

    public static class Alerts implements BaseColumns {
        public static final String TABLE_NAME = "alerts";
        public static final String COLUMN_ID = "ID";
        public static final String COLUMN_PARAM_SET_ID = "parameter_set_id";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_MESSAGE = "message";
    }

    protected static final String SQL_CREATE_MEASUREMENT =
            "CREATE TABLE " + Measurements.TABLE_NAME + " (" +
                    Measurements.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Measurements.COLUMN_DATE + " TIMESTAMP, " +
                    Measurements.COLUMN_VALUE + " DOUBLE, " +
                    Measurements.COLUMN_SENSOR_ID + " VARCHAR(4), " +
                    Measurements.COLUMN_ZONE_ID + " INT, " +
                    Measurements.COLUMN_IS_CORRECT + " DOUBLE " +
                    ")";

    protected static final String SQL_CREATE_ALERT =
            "CREATE TABLE " + Alerts.TABLE_NAME + " (" +
                    Alerts.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Alerts.COLUMN_PARAM_SET_ID + " INT, " +
                    Alerts.COLUMN_CREATED_AT + " TIMESTAMP, " +
                    Alerts.COLUMN_MESSAGE + " VARCHAR(128) " +
                    ")";

    protected static final String SQL_DELETE_ALERT_DATA = "DELETE FROM " + Alerts.TABLE_NAME;
    protected static final String SQL_DELETE_MEASUREMENT_DATA = "DELETE FROM " + Measurements.TABLE_NAME;

    protected static final String SQL_CREATE_DROP_ALERT_IFEXISTS = "DROP TABLE IF EXISTS " + Alerts.TABLE_NAME;
    protected static final String SQL_CREATE_DROP_MEASUREMENT_IFEXISTS = "DROP TABLE IF EXISTS " + Measurements.TABLE_NAME;

}
