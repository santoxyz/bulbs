package com.santox.bulbs;

import android.support.constraint.solver.ArrayLinkedVariables;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

/**
 0x60c7,"192.168.11.10", "9b99d8a7df24"
 0x60c7,"192.168.11.8", "cf99d8a7df24"
 0x60c7,"192.168.11.9", "85a2d8a7df24"
 0x60c7,"192.168.11.9", "85a2d8a7df24"
 */

public class MainActivity extends AppCompatActivity {
    Button btnIngresso;
    Button btnSoggiorno;
    ArrayList<Bulb> bulbsIngresso = new ArrayList<>();
    ArrayList<Bulb> bulbsSoggiorno = new ArrayList<>();
    boolean state_ingresso = false;
    boolean state_soggiorno = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnIngresso = (Button) findViewById(R.id.button_ingresso);
        btnSoggiorno = (Button) findViewById(R.id.button_soggiorno);

        bulbsIngresso.add(new Bulb(0x60c7,"192.168.11.10", "9b99d8a7df24"));
        bulbsSoggiorno.add(new Bulb(0x60c7,"192.168.11.8", "cf99d8a7df24"));
        bulbsSoggiorno.add(new Bulb(0x60c7,"192.168.11.9", "85a2d8a7df24"));
        bulbsSoggiorno.add(new Bulb(0x60c7,"192.168.11.9", "85a2d8a7df24"));

        btnSoggiorno.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Bulb b: bulbsSoggiorno) {
                    b.auth();
                    state_soggiorno = !state_soggiorno;
                    b.set_state(state_soggiorno);
                }
            }
        });
    }



}
