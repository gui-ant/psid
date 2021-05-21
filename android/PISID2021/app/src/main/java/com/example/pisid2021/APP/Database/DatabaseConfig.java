package com.example.pisid2021.APP.Database;

import android.provider.BaseColumns;

public class DatabaseConfig {

    public static class Measurement implements BaseColumns {
        public static final String TABLE_NAME = "measurements";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_VALUE = "value";
    }

    public static class Alert implements BaseColumns {
        public static final String TABLE_NAME = "alerts";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_CREATED_AT = "created_at";
        public static final String COLUMN_MESSAGE = "message";
    }


    protected static final String SQL_CREATE_MEASUREMENT =
            "CREATE TABLE " + Measurement.TABLE_NAME + " (" +
                    Measurement.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Measurement.COLUMN_DATE + " TIMESTAMP, " +
                    Measurement.COLUMN_VALUE + " DOUBLE, " +
                    ")";

    protected static final String SQL_CREATE_ALERT =
            "CREATE TABLE " + Alert.TABLE_NAME + " (" +
                    Alert.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    Alert.COLUMN_CREATED_AT + " TIMESTAMP, " +
                    Alert.COLUMN_MESSAGE + " VARCHAR(128) " +
                    ")";

    protected static final String SQL_DELETE_ALERT_DATA = "DELETE FROM " + Alert.TABLE_NAME;
    protected static final String SQL_DELETE_MEASUREMENT_DATA = "DELETE FROM " + Measurement.TABLE_NAME;

    protected static final String SQL_CREATE_DROP_ALERT_IFEXISTS = "DROP TABLE IF EXISTS " + Alert.TABLE_NAME;
    protected static final String SQL_CREATE_DROP_MEASUREMENT_IFEXISTS = "DROP TABLE IF EXISTS " + Measurement.TABLE_NAME;

}
