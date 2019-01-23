package com.example.cubebyes;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

public class ByesActivity extends AppCompatActivity {

    private HashSet<CharSequence> selectedPlayers = new HashSet<>();
    private TreeMap<CharSequence, Integer> playerAffinities = new TreeMap<>();
    private List<CheckBox> boxes = new ArrayList<>();
    private static final Random rand = new Random();
    public static final int r1EligibleCount = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_byes);

        Button b = findViewById(R.id.byeButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateLaterBye();
            }
        });
        b.setEnabled(false);

        Intent intent = getIntent();
        Object o = intent.getExtras().get("players");

        HashMap<CharSequence, Integer> incomingPlayers = (HashMap<CharSequence, Integer>) o;

        List<Player> rankedPlayers = new ArrayList<>(incomingPlayers.size());
        for (Map.Entry<CharSequence, Integer> e : incomingPlayers.entrySet()) {
            playerAffinities.put(e.getKey(), e.getValue());
            rankedPlayers.add(new Player(e.getKey(), e.getValue()));
        }

        populateList();
        generateFirstBye(rankedPlayers);
        
    }

    
    void generateFirstBye(List<Player> rankedPlayers) {
        Collections.sort(rankedPlayers);
        List<Player> eligiblePlayers = null;
        if (rankedPlayers.size() > r1EligibleCount) {
            int bar = rankedPlayers.get(r1EligibleCount-1).affinity;
            int rangeEnd = r1EligibleCount;
            for(int i = r1EligibleCount; i < rankedPlayers.size(); ++i) {
                if(rankedPlayers.get(i).affinity < bar) {
                    rangeEnd = i;
                    break;
                }
            }
            eligiblePlayers = rankedPlayers.subList(0, rangeEnd);
        } else {
            eligiblePlayers = rankedPlayers;
        }

        //pick among eligible players weighted by their affinities
        if(eligiblePlayers != null && eligiblePlayers.size() > 0) {
            int totalAffinity = 0;
            for (Player p : eligiblePlayers) {
                totalAffinity += p.affinity;
            }

            int num = rand.nextInt(totalAffinity);
            for(Player p : eligiblePlayers) {
                num -= p.affinity;
                if (num < 0) {
                    announceBye(p.name);
                    break;
                }
            }
        }
    }

    void generateLaterBye() {
        LinearLayout playerList = findViewById(R.id.playerList);
        Log.d("byes","going through boxes");
        
        List<Player> eligiblePlayers = new ArrayList<>();
        for(CharSequence name : selectedPlayers) {
            eligiblePlayers.add(new Player(name, playerAffinities.get(name)));
        }
        selectedPlayers.clear();

        Collections.shuffle(eligiblePlayers);

        int topAffinity = -999;
        CharSequence byeGetter = "";
        for (Player p : eligiblePlayers) {
            if (p.affinity > topAffinity) {
                topAffinity = p.affinity;
                byeGetter = p.name;
            }
        }

        announceBye(byeGetter);
        for(CheckBox cb : boxes) {
            cb.setChecked(false);
        }

    }

    void announceBye(CharSequence name) {
        CharSequence text;
        if(name == null || name.length() == 0) {
            text = "Error: no name provided";
        } else {
            text = name + " gets the bye";
        }
        TextView t = findViewById(R.id.byeText);
        t.setText(text);
    }

    void populateList() {
        LinearLayout playerList = findViewById(R.id.playerList);
        for (Map.Entry<CharSequence, Integer> e : playerAffinities.entrySet()) {
            CharSequence name = e.getKey();
            int affinity = e.getValue();
            CheckBox cb = createBox(name);
            cb.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            cb.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
            boxes.add(cb);
            
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
                Button b = findViewById(R.id.byeButton);
                if (selectedPlayers.size() == 0) {
                    b.setEnabled(false);
                } else {
                    b.setEnabled(true);
                }
            }
        });
        return cb;
    }
}
