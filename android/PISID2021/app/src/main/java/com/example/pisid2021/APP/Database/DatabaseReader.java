package com.example.pisid2021.APP.Database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseReader {
    SQLiteDatabase db;

    public DatabaseReader(DatabaseHandler dbHandler){
        db = dbHandler.getReadableDatabase();
    }

    public Cursor readMedicoes(){
        Cursor cursor = db.query(
                DatabaseConfig.Medicao.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DatabaseConfig.Medicao.COLUMN_NAME_HORA + " ASC"
        );
        return cursor;
    }

    public Cursor readAlertas(){
        Cursor cursor = db.query(
                DatabaseConfig.Alerta.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                DatabaseConfig.Alerta.COLUMN_NAME_HORA + " DESC"
        );
        return cursor;
    }
}
