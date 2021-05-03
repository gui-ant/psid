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
        sqLiteDatabase.execSQL(config.SQL_CREATE_DROP_MEDICAO_IFEXISTS);
        sqLiteDatabase.execSQL(config.SQL_CREATE_MEDICAO);
        sqLiteDatabase.execSQL(config.SQL_CREATE_DROP_ALERTA_IFEXISTS);
        sqLiteDatabase.execSQL(config.SQL_CREATE_ALERTA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }

    public void insertMedicao(String hora, double leitura) {
        ContentValues values = new ContentValues();
        values.put(DatabaseConfig.Medicao.COLUMN_NAME_HORA, hora);
        values.put(DatabaseConfig.Medicao.COLUMN_NAME_LEITURA, leitura);
        getWritableDatabase().insert(DatabaseConfig.Medicao.TABLE_NAME,null, values);
    }

    public void insertAlerta(String zona, String sensor, String hora, double leitura, String tipoAlerta, String cultura, String mensagem, int idUtilizador, int idCultura, String horaEscrita) {
        ContentValues values = new ContentValues();
        values.put(DatabaseConfig.Alerta.COLUMN_NAME_ZONA, zona);
        values.put(DatabaseConfig.Alerta.COLUMN_NAME_SENSOR, sensor);
        values.put(DatabaseConfig.Alerta.COLUMN_NAME_HORA, hora);
        values.put(DatabaseConfig.Alerta.COLUMN_NAME_LEITURA, leitura);
        values.put(DatabaseConfig.Alerta.COLUMN_NAME_TIPO_ALERTA, tipoAlerta);
        values.put(DatabaseConfig.Alerta.COLUMN_NAME_CULTURA, cultura);
        values.put(DatabaseConfig.Alerta.COLUMN_NAME_MENSAGEM, mensagem);
        values.put(DatabaseConfig.Alerta.COLUMN_NAME_ID_UTILIZADOR, idUtilizador);
        values.put(DatabaseConfig.Alerta.COLUMN_NAME_ID_CULTURA, idCultura);
        values.put(DatabaseConfig.Alerta.COLUMN_NAME_HORA_ESCRITA, horaEscrita);
        getWritableDatabase().insert(DatabaseConfig.Alerta.TABLE_NAME,null, values);
    }

    public void clearAlertas() { getWritableDatabase().execSQL(config.SQL_DELETE_ALERTA_DATA); }

    public void clearMedicoes() {
        getWritableDatabase().execSQL(config.SQL_DELETE_MEDICAO_DATA);
    }

}
