package com.example.damiano.treasurehunt;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.widget.TextView;
import android.widget.ImageView;
import android.view.View;

import java.util.concurrent.TimeUnit;

public class FinalResult extends AppCompatActivity {
    private String idEvent;
    private BackgroundTask getResultTask = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String resp=null;//new String();
        System.out.println("create finalresult");
        setContentView(R.layout.activity_final_result);
        String[] label;
        final TextView textposition = (TextView) findViewById(R.id.positiontext);
        final TextView textpoints = (TextView) findViewById(R.id.pointtext);
        final TextView texttotal = (TextView) findViewById(R.id.totaltext);
        final TextView title = (TextView) findViewById(R.id.title);

        idEvent=getIntent().getExtras().getString("idEvent");
        getResultTask = new BackgroundTask("{'message_type':'10',\n"+ "'idEvent': '" + idEvent + "'}");
        getResultTask.execute((Void) null); // run in background
        try {
            getResultTask.get(10, TimeUnit.SECONDS);
            resp=getResultTask.getResult();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (resp.length()==0) {
            System.out.println("rispsota vuota!");
        }
        System.out.println(resp);
        label= resp.split(",");


        textposition.setText(label[0]);
        textpoints.setText(label[1]);
        texttotal.setText(label[2]);
        System.out.println("label1 : "+ label[1]);
        if (label[1].equals("+0"))
            title.setText("LOSER:");
        ImageView imgButton =(ImageView)findViewById(R.id.imageView);
        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FinalResult.this, ListActivity.class));

            }
        });
    }

}
