package com.example.pisid2021.APP;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.pisid2021.APP.Connection.ConnectionHandler;
import com.example.pisid2021.APP.Database.DatabaseHandler;
import com.example.pisid2021.APP.Database.DatabaseReader;
import com.example.pisid2021.APP.Helper.UserLogin;
import com.example.pisid2021.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class MedicoesActivity extends AppCompatActivity {

    private static final String IP = UserLogin.getInstance().getIp();
    private static final String PORT = UserLogin.getInstance().getPort();
    private static final String username= UserLogin.getInstance().getUsername();
    private static final String password = UserLogin.getInstance().getPassword();
    private static String selectedZone = "-1";
    private static Spinner zoneSpinner;
    private static ArrayAdapter<String> spinnerAdapter;
    private static int zonesState = 0;
    private static boolean firstTime;
    String getMedicoes = "http://" + IP + ":" + PORT + "/scripts/getMedicoesTemperatura.php";
    DatabaseHandler db = new DatabaseHandler(this);

    Handler h = new Handler();
    int delay = 1000; //1 second=1000 milisecond
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firstTime=true;
        setContentView(R.layout.activity_medicoes);
        zoneSpinner = findViewById(R.id.spinner1);
        List<String> zonesList = new ArrayList<>();
        zonesList.add("Waiting for data...");

        spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, zonesList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        zoneSpinner.setAdapter(spinnerAdapter);
        spinnerAdapter.notifyDataSetChanged();

        zoneSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(parent.getItemAtPosition(position).toString().contains("Zona"))
                    selectedZone = parent.getItemAtPosition(position).toString().split(" ")[1];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        updateMedicoes();
        drawGraphs();
    }

    @Override
    protected void onResume() {
        //start handler as activity become visible
        h.postDelayed( runnable = new Runnable() {
            public void run() {
                updateMedicoes();
                drawGraphs();

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

    public void alertas(View v){
        Intent i = new Intent(this, AlertasActivity.class);
        startActivity(i);
    }

    private void updateMedicoes(){
        db.clearMeasurements();
        HashMap<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        params.put("selectedZone", selectedZone);
        ConnectionHandler jParser = new ConnectionHandler();
        JSONArray medicoes = jParser.getJSONFromUrl(getMedicoes, params);
        try {
            if (medicoes != null){

                JSONObject c = medicoes.getJSONObject(0);
                JSONArray zones = c.getJSONArray("zonas");


                // Código responsável por atualizar a lista com as zonas disponíveis, só atualiza até que seja selecionada uma das zonas disponíveis.
                if(selectedZone == "-1" || selectedZone == "Waiting for data..." || zones.length()!=zonesState || firstTime) {


                    ArrayList<String> zoneslist = new ArrayList<String>();

                    if (zones != null) {
                        int len = zones.length();
                        for (int i = 0; i < len; i++) {
                            zoneslist.add("Zona "+zones.get(i).toString());
                        }
                    }

                    spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, zoneslist);
                    zoneSpinner.setAdapter(spinnerAdapter);
                    spinnerAdapter.notifyDataSetChanged();

                    zonesState = zones.length();

                }
                firstTime = false;
                for (int i=0;i< medicoes.length();i++){
                    c = medicoes.getJSONObject(i);
                    String hora = c.getString("date");
                    System.out.println("Hora a inserir: " + hora);
                    double leitura;
                    try {
                        leitura = c.getDouble("value");
                        System.out.println("Valor a inserir: "+leitura);
                    } catch (Exception e) {
                        leitura = -1000.0;
                    }
                    db.insertMeasurement(hora, leitura);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void drawGraphs(){
        GraphView graphTemperatura = findViewById(R.id.temperatura_graph);
        graphTemperatura.removeAllSeries();
        int helper=0;
        DatabaseReader dbReader = new DatabaseReader(db);
        Cursor cursorTemperatura = dbReader.readMedicoes();
        Date currentTimestamp = new Date();
        long currentLong = currentTimestamp.getTime();

        DataPoint[] datapointsTemperatura = new DataPoint[cursorTemperatura.getCount()];

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        while (cursorTemperatura.moveToNext()){
            String hora =  cursorTemperatura.getString(cursorTemperatura.getColumnIndex("date"));
            Integer valorMedicao = cursorTemperatura.getInt(cursorTemperatura.getColumnIndex("value"));
            try {
                Date date = format.parse(hora);
                long pointLong = date.getTime();
                long difference = currentLong - pointLong;
                double seconds = 300 - TimeUnit.MILLISECONDS.toSeconds(difference);
                datapointsTemperatura[helper]=new DataPoint(seconds,valorMedicao);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            helper++;
        }
        cursorTemperatura.close();

        graphTemperatura.getViewport().setXAxisBoundsManual(true);
        graphTemperatura.getViewport().setMinX(0);
        graphTemperatura.getViewport().setMaxX(300);
        LineGraphSeries<DataPoint> seriesTemperatura = new LineGraphSeries<>(datapointsTemperatura);

        seriesTemperatura.setColor(Color.RED);
        if(selectedZone != "-1"){
            if(Integer.parseInt(selectedZone)%2==0){
                seriesTemperatura.setColor(Color.RED);
            }else{
                seriesTemperatura.setColor(Color.BLUE);
            }
        }

        seriesTemperatura.setTitle("Temperatura");
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graphTemperatura);
        staticLabelsFormatter.setHorizontalLabels(new String[] {"300"," 250", "200", "150", "100", "50", "0"});
        graphTemperatura.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
        graphTemperatura.getLegendRenderer().setVisible(true);
        graphTemperatura.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
        graphTemperatura.getLegendRenderer().setBackgroundColor(Color.alpha(0));
        graphTemperatura.addSeries(seriesTemperatura);
    }

}
