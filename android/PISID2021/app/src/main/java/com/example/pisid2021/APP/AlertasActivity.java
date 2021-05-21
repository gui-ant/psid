package com.example.pisid2021.APP;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.pisid2021.APP.Connection.ConnectionHandler;
import com.example.pisid2021.APP.Database.DatabaseHandler;
import com.example.pisid2021.APP.Database.DatabaseReader;
import com.example.pisid2021.APP.Helper.UserLogin;
import com.example.pisid2021.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class AlertasActivity extends AppCompatActivity {

    private static final String IP = UserLogin.getInstance().getIp();
    private static final String PORT = UserLogin.getInstance().getPort();
    private static final String username= UserLogin.getInstance().getUsername();
    private static final String password = UserLogin.getInstance().getPassword();
    DatabaseHandler db = new DatabaseHandler(this);
    String getAlertas = "http://" + IP + ":" + PORT + "/scripts/getAlertasGlobais.php";
    int year;
    int month;
    int day;
    String date;

    Handler h = new Handler();
    int delay = 1000; //1 second=1000 milisecond
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alertas);

        if (getIntent().hasExtra("date")){
            int[] yearMonthDay = getIntent().getIntArrayExtra("date");
            year = yearMonthDay[0];
            month= yearMonthDay[1];
            day=yearMonthDay[2];
        } else {
            year = Calendar.getInstance().get(Calendar.YEAR);
            month = Calendar.getInstance().get(Calendar.MONTH)+1;
            day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        }
        dateToString();
        getAlertas();
        listAlertas();
    }

    private void dateToString() {
       String yearString = Integer.toString(year);
       String monthString ="";
       String dayString="";
        if (month<10){
            monthString="0"+Integer.toString(month);
        } else {
            monthString=Integer.toString(month);
        }
        if(day<10){
            dayString="0"+Integer.toString(day);
        } else {
            dayString=Integer.toString(day);
        }
        date = yearString+"-"+monthString+"-"+dayString;
        String formatted_date = dayString+"-"+monthString+"-"+yearString;
        TextView stringData = findViewById(R.id.diaSelecionado_tv);
        stringData.setText(formatted_date);

    }

    public void showDatePicker(View v) {
        Intent intent = new Intent(this,DatePickerActivity.class);
        intent.putExtra("global",1);
        startActivity(intent);
        finish();
    }

    private void getAlertas() {
        db.clearAlerts();
        HashMap<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        params.put("date",date);
        ConnectionHandler jParser = new ConnectionHandler();
        JSONArray alertas = jParser.getJSONFromUrl(getAlertas, params);
        try{
            if(alertas!=null){
                for (int i=0;i< alertas.length();i++){
                    JSONObject c = alertas.getJSONObject(i);
                    String mensagem = c.getString("message");
                    String horaEscrita = c.getString("created_at");
                    db.insertAlert(mensagem, horaEscrita);
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void listAlertas() {
        SharedPreferences sp = getApplicationContext().getSharedPreferences("appPref", MODE_PRIVATE);
        int mostRecentEntry = 0;

        TableLayout table = findViewById(R.id.tableAlertas);

        DatabaseReader dbReader = new DatabaseReader(db);
        Cursor cursorAlertas = dbReader.readAlertas();
        table.removeAllViewsInLayout();
        TableRow headerRow = new TableRow(this);
        headerRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        List<String> alertaFields = Arrays.asList(new String[]{"Hora do alerta", "Mensagem"});

        for (String field: alertaFields) {
            TextView header = new TextView(this);
            header.setText(field);
            header.setTextSize(16);
            header.setPadding(dpAsPixels(16),dpAsPixels(50),0,10);

            headerRow.addView(header);
        }

        table.addView(headerRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        while (cursorAlertas.moveToNext()){
            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            TextView hora = new TextView(this);
            String valorHora = cursorAlertas.getString(cursorAlertas.getColumnIndex("created_at")).split(" ")[1];
            hora.setText(valorHora);
            hora.setPadding(dpAsPixels(16),dpAsPixels(5),0,0);

            TextView mensagem = new TextView(this);
            String valorMensagem = cursorAlertas.getString(cursorAlertas.getColumnIndex("message"));
            if (valorMensagem == null || valorMensagem.equals("null")) valorMensagem = "";
            mensagem.setText(valorMensagem);
            mensagem.setPadding(dpAsPixels(16),dpAsPixels(5),dpAsPixels(5),0);

            String intHora = valorHora.replace(":", "");
            int newHora = Integer.parseInt(intHora);
            if (newHora > mostRecentEntry) mostRecentEntry = newHora;
            if (newHora > sp.getInt("timePref", 0)) {
                if (sp.getInt("refreshPref", 1) == 0) {
                    hora.setTextColor(Color.RED);
                    mensagem.setTextColor(Color.RED);
                }
            }
            row.addView(hora);
            row.addView(mensagem);

            table.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }
        SharedPreferences.Editor editor = sp.edit().putInt("timePref", mostRecentEntry);
        editor.apply();
        SharedPreferences.Editor editor2 = sp.edit().putInt("refreshPref", 0);
        editor2.apply();
    }

    private int dpAsPixels(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp*scale + 0.5f);
    }

    public void medicoes(View v) {
        Intent i = new Intent(this, MedicoesActivity.class);
        startActivity(i);
    }

    @Override
    protected void onResume() {
        //start handler as activity become visible
        h.postDelayed( runnable = new Runnable() {
            public void run() {
                getAlertas();
                listAlertas();

                h.postDelayed(runnable, delay);
            }
        }, delay);

        super.onResume();
    }

    @Override
    protected void onPause() {
        h.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }
}
