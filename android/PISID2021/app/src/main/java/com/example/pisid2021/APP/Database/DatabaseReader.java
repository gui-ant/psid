package com.example.pisid2021.APP.Database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseReader {
    SQLiteDatabase db;

    public DatabaseReader(DatabaseHandler dbHandler) {
        db = dbHandler.getReadableDatabase();
    }

    public Cursor readMedicoes() {
        Cursor cursor = db.query(
                DatabaseConfig.Measurement.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DatabaseConfig.Measurement.COLUMN_DATE + " ASC"
        );
        return cursor;
    }

    public Cursor readAlertas() {
        Cursor cursor = db.query(
                DatabaseConfig.Alert.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DatabaseConfig.Alert.COLUMN_CREATED_AT + " DESC"
        );
        return cursor;
    }
}
