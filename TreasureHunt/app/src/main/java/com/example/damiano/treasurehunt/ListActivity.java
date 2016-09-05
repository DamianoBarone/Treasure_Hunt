package com.example.damiano.treasurehunt;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.damiano.treasurehunt.R;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.net.Uri;

import android.widget.TextView;

public class ListActivity extends AppCompatActivity {

    private BackgroundTask getEventList = null;
    String[] elements;
    String[] supp;
    String supp1;
    private ArrayList<String> event_list=null;
    private ArrayAdapter<String> adapter=null;
    private TextView textname=null;
    private TextView textpoints=null;
    private SwipeRefreshLayout mSwipeRefreshLayout=null;

    private void updateInfo(boolean firstUpdate) {
        //get element from servlet
        String eventList=null;//new String();
        getEventList = new BackgroundTask("{'message_type':'3',\n}");
        getEventList.execute((Void) null); // run in background
        try {
            getEventList.get(10, TimeUnit.SECONDS);
            eventList=getEventList.getResult();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (eventList.length()==0) {
            System.out.println("lista venti vuota!");
        }
        supp=eventList.split(";");
        supp1= eventList.substring(supp[0].length()+1,eventList.length());
        System.out.println(supp[0].length()+ " lunghezza "+supp1);
        elements=supp1.split(";");
        String[] fields=null;
        String[] title =new String(supp[0]).split(",");
        System.out.println("titolo :" + title[0] +title[1]);
        textname.setText(title[0]);
        textpoints.setText(title[1]);

        // creo lista elementi
        if (event_list==null) // it means that firstUpdate=true
            event_list = new ArrayList<String>();
        else // it means that firstUpdate=false
            event_list.clear();

        for (int i = 0; i < elements.length; ++i) {
            fields=new String(elements[i]).split(",");
            event_list.add(new String(fields[0] + "\nstart: " +fields[2] + "\nend: "+ fields[3])); // display only name
            //System.out.println("Scrivo: "+new String(fields[0])+new String(fields[1])+new String(fields[2])+new String(fields[3]));

            System.out.println("Scrivo: "+new String(fields[0]));
            System.out.println("Tutta: "+new String(elements[i]));

            //Snackbar.make(this, , Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }

        if (firstUpdate==false)
            adapter.notifyDataSetChanged();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        textname = (TextView) findViewById(R.id.text_name);
        textpoints = (TextView) findViewById(R.id.text_points);
        mSwipeRefreshLayout=(SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateInfo(false);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        updateInfo(true);

        // recupero la lista dal layout
        final ListView mylist = (ListView) findViewById(R.id.listView1);

        // creo e istruisco l'adattatore
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, event_list);

        // inietto i dati
        mylist.setAdapter(adapter);

        mylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adattatore, final View componente, int pos, long id){
                // recupero il titolo memorizzato nella riga tramite l'ArrayAdapter
                final String titoloriga = (String) adattatore.getItemAtPosition((int) id);
                System.out.println("----------- 1: "+titoloriga+", "+pos+", "+id);
                //Log.d("List", "Ho cliccato sull'elemento con titolo" + titoloriga);
                //Snackbar.make(componente, "Switch to event"+titoloriga, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                double lat=Double.parseDouble(new String(elements[(int) id]).split(",")[4]);
                double lon=Double.parseDouble(new String(elements[(int) id]).split(",")[5]);
                String idEvent=(new String(elements[(int) id]).split(",")[6]);
//MODIFCHE PER TEST DELLA RA
                Intent map=new Intent(ListActivity.this, MapsActivity.class);
                map.putExtra("lat", lat);
                map.putExtra("lon", lon);

                map.putExtra("idEvent", idEvent);
                map.putExtra("date_start", new String(elements[(int) id]).split(",")[2]);
                map.putExtra("date_end", new String(elements[(int) id]).split(",")[3]);
                System.out.println("----------- 2: "+lat+", "+lon+", "+idEvent);

                startActivity(map);

                //Intent viewCamera=new Intent(ListActivity.this, CameraViewActivity.class);
                //startActivity(viewCamera);


            }
        });

        Button b= (Button) findViewById(R.id.add_button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // agiungi qui elementi
                //event_list.add("Element_add");
                //adapter.notifyDataSetChanged();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                //event_list.remove(event_list.size()-1);
                //adapter.notifyDataSetChanged();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

}


