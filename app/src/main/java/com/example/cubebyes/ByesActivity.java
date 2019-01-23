package com.example.cubebyes;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

public class ByesActivity extends AppCompatActivity {

    private HashSet<CharSequence> selectedPlayers = new HashSet<>();
    private TreeMap<CharSequence, Integer> playerAffinities = new TreeMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_byes);
        Intent intent = getIntent();
        Object o = intent.getExtras().get("players");

        HashMap<CharSequence, Integer> incomingPlayers = (HashMap<CharSequence, Integer>) o;
        for (Map.Entry<CharSequence, Integer> e : incomingPlayers.entrySet()) {
            playerAffinities.put(e.getKey(), e.getValue());
        }

        populateList();

    }


    void populateList() {
        LinearLayout playerList = findViewById(R.id.playerList);
        for (Map.Entry<CharSequence, Integer> e : playerAffinities.entrySet()) {
            CharSequence name = e.getKey();
            int affinity = e.getValue();
            CheckBox cb = createBox(name);
            cb.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            cb.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));

            TextView t = new TextView(getApplicationContext());
            t.setText("" + affinity);
            t.setTextSize(32);
            t.setGravity(Gravity.RIGHT);

            LinearLayout ll = new LinearLayout(getApplicationContext());
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.addView(cb);
            ll.addView(t);
            playerList.addView(ll);
        }
    }

    CheckBox createBox(CharSequence label) {
        CheckBox cb = new CheckBox(getApplicationContext());
        cb.setText(label);
        cb.setTextSize(32);
        cb.setTextColor(Color.DKGRAY);
        cb.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                CharSequence text = buttonView.getText();
                if (text == null) return;
                if(isChecked) {
                    selectedPlayers.add(text);
                } else {
                    selectedPlayers.remove(text);
                }
            }
        });
        return cb;
    }
}
