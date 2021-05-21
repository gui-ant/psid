package com.example.pisid2021.APP.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "PISID.db";
    DatabaseConfig config = new DatabaseConfig();

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(config.SQL_CREATE_DROP_MEASUREMENT_IFEXISTS);
        sqLiteDatabase.execSQL(config.SQL_CREATE_MEASUREMENT);
        sqLiteDatabase.execSQL(config.SQL_CREATE_DROP_ALERT_IFEXISTS);
        sqLiteDatabase.execSQL(config.SQL_CREATE_ALERT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void insertMeasurement(String hora, double leitura) {
        ContentValues values = new ContentValues();
        values.put(DatabaseConfig.Measurement.COLUMN_DATE, hora);
        values.put(DatabaseConfig.Measurement.COLUMN_VALUE, leitura);
        getWritableDatabase().insert(DatabaseConfig.Measurement.TABLE_NAME, null, values);
    }

    public void insertAlert(String mensagem,String horaEscrita) {

        ContentValues values = new ContentValues();
        values.put(DatabaseConfig.Alert.COLUMN_MESSAGE, mensagem);
        values.put(DatabaseConfig.Alert.COLUMN_CREATED_AT, horaEscrita);
        getWritableDatabase().insert(DatabaseConfig.Alert.TABLE_NAME, null, values);
    }

    public void clearAlerts() {
        getWritableDatabase().execSQL(config.SQL_DELETE_ALERT_DATA);
    }

    public void clearMeasurements() {
        getWritableDatabase().execSQL(config.SQL_DELETE_MEASUREMENT_DATA);
    }

}
