package com.example.pisid2021.APP.Database;

import android.provider.BaseColumns;

public class DatabaseConfig {

    public static class Medicao implements BaseColumns {
        public static final String TABLE_NAME="Medicao";
        public static final String COLUMN_NAME_ID_MEDICAO ="IDMedicao";
        public static final String COLUMN_NAME_HORA ="Hora";
        public static final String COLUMN_NAME_LEITURA ="Leitura";
    }

    public static class Alerta implements BaseColumns {
        public static final String TABLE_NAME="Alerta";
        public static final String COLUMN_NAME_ID_ALERTA ="IDAlerta";
        public static final String COLUMN_NAME_ZONA ="Zona";
        public static final String COLUMN_NAME_SENSOR ="Sensor";
        public static final String COLUMN_NAME_HORA ="Hora";
        public static final String COLUMN_NAME_LEITURA ="Leitura";
        public static final String COLUMN_NAME_TIPO_ALERTA ="TipoAlerta";
        public static final String COLUMN_NAME_CULTURA ="Cultura";
        public static final String COLUMN_NAME_MENSAGEM ="Mensagem";
        public static final String COLUMN_NAME_ID_UTILIZADOR ="IDUtilizador";
        public static final String COLUMN_NAME_ID_CULTURA ="IDCultura";
        public static final String COLUMN_NAME_HORA_ESCRITA ="HoraEscrita";
    }

    protected static final String SQL_CREATE_MEDICAO =
            "CREATE TABLE " + Medicao.TABLE_NAME +
                    " (" + Medicao.COLUMN_NAME_ID_MEDICAO + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Medicao.COLUMN_NAME_HORA + " TIMESTAMP," +
                    Medicao.COLUMN_NAME_LEITURA + " DOUBLE  )";

    protected static final String SQL_DELETE_MEDICAO_DATA =
            "DELETE FROM " + Medicao.TABLE_NAME;

    protected static final String SQL_CREATE_ALERTA =
            "CREATE TABLE " + Alerta.TABLE_NAME +
                    " (" + Alerta.COLUMN_NAME_ID_ALERTA + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    Alerta.COLUMN_NAME_ZONA + " TEXT," +
                    Alerta.COLUMN_NAME_SENSOR + " TEXT, " +
                    Alerta.COLUMN_NAME_HORA + " TIMESTAMP," +
                    Alerta.COLUMN_NAME_LEITURA + " DOUBLE," +
                    Alerta.COLUMN_NAME_TIPO_ALERTA + " TEXT," +
                    Alerta.COLUMN_NAME_CULTURA + " TEXT," +
                    Alerta.COLUMN_NAME_MENSAGEM + " TEXT," +
                    Alerta.COLUMN_NAME_ID_UTILIZADOR + " INTEGER," +
                    Alerta.COLUMN_NAME_ID_CULTURA + " INTEGER," +
                    Alerta.COLUMN_NAME_HORA_ESCRITA + " TIMESTAMP )";

    protected static final String SQL_DELETE_ALERTA_DATA =
            "DELETE FROM " + Alerta.TABLE_NAME;

    protected static final String SQL_CREATE_DROP_ALERTA_IFEXISTS=("DROP TABLE IF EXISTS ") + Alerta.TABLE_NAME;
    protected static final String SQL_CREATE_DROP_MEDICAO_IFEXISTS =("DROP TABLE IF EXISTS ") + Medicao.TABLE_NAME;

}
