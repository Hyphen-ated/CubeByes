package io.github.hyphenated.cubebyes;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.*;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private HashSet<String> selectedPlayers = new HashSet<>();
    private TreeMap<String, Integer> playerAffinities = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    private TextView playerCountText;

    private RequestQueue requestQueue;
    
    public static final String playerFileName = "players.txt";
    public static final String githubUrl = "https://raw.githubusercontent.com/Hyphen-ated/CubeByeAffinities/master/players.txt";

    
    
    private void snackMsg(String msg) {
        Snackbar bar = Snackbar.make(playerCountText, msg, 2000);
        bar.show();
    }
    
    private void snackErr(String msg, Throwable tr) {
        snackMsg(msg);
        Log.e("cubebyes", msg, tr);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestQueue = Volley.newRequestQueue(this);

        setupPlayersFromFile();

        FloatingActionButton startCube = findViewById(R.id.startCubeButton);
        final MainActivity that = this;
        startCube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String, Integer> activePlayers = new HashMap<>();
                for (String player : selectedPlayers) {
                    activePlayers.put(player, playerAffinities.get(player));
                }
                Intent intent = new Intent(that, ByesActivity.class);
                intent.putExtra("players", activePlayers);
                startActivity(intent);
            }
        });

        FloatingActionButton refresh = findViewById(R.id.refreshButton);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatePlayerData();
            }
        });

        playerCountText = findViewById(R.id.playerCount);
    }

    
    
    
    //returns true if an update occurred
    private void updatePlayerData() {
        StringRequest req = new StringRequest(Request.Method.GET, githubUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String oldData = null;
                try {
                    FileInputStream fis = openFileInput(playerFileName);
                    oldData = IOUtils.toString(fis, "utf-8");
                    if(response.equals(oldData)) {
                        snackMsg("Player data already up to date");
                        return;
                    }
                } catch (FileNotFoundException e) {
                    //if we couldn't find the file, there is no old data and that's fine
                } catch (IOException e) {
                    //if we failed to read the old file, fine, ignore it
                }

                File dir = getFilesDir();
                File playersFile = new File(dir, playerFileName);
                try {
                    IOUtils.write(response, new FileOutputStream(playersFile));
                } catch (IOException e) {
                    snackErr("Couldn't write player data to a file", e);
                    return;
                }
                
                if(setupPlayersFromFile()) {
                    snackMsg("Updated player data from the internet");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String msg;
                if (error instanceof NoConnectionError ) {
                    msg = "You're not connected to the internet";
                } else if (error instanceof ServerError) {
                    msg = "Bye data server gave us an unexpected error";
                } else if (error instanceof TimeoutError || error instanceof NetworkError) {
                    msg = "Unable to connect to the bye data server";
                } else {
                    msg = "Unknown error";
                }
                snackErr(msg, error);
            }
        });
        
        requestQueue.add(req);
        return;
    }

    private boolean setupPlayersFromFile() {
        List<String> lines;
        try {
            FileInputStream fis = openFileInput(playerFileName);
            lines = IOUtils.readLines(fis, "utf-8");
        } catch (FileNotFoundException e) {
            try {
                lines = IOUtils.readLines(new StringReader("0 No\n 0 player data\n0 present"));
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
            //"Couldn't find players.txt"
        } catch (IOException e) {
            snackErr("Failed to read local players file", e);
            return false;
        }


        Pattern pat = Pattern.compile("^\\s*(\\d*)\\s+(.+)");
        LinearLayout playerList = findViewById(R.id.playerList);
        playerList.removeAllViews();
        playerAffinities.clear();
        selectedPlayers.clear();
        for (String s : lines) {
            Matcher m = pat.matcher(s);
            if(m.matches()) {
                int affinity = Integer.parseInt(m.group(1));
                String name = m.group(2);
                playerAffinities.put(name, affinity);
            }

        }


        populateList();
        return true;
    }

    void populateList() {
        LinearLayout playerList = findViewById(R.id.playerList);
        for (Map.Entry<String, Integer> e : playerAffinities.entrySet()) {
            String name = e.getKey();
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

    CheckBox createBox(String label) {
        CheckBox cb = new CheckBox(getApplicationContext());
        cb.setText(label);
        cb.setTextSize(32);
        cb.setTextColor(Color.DKGRAY);
        cb.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String text = buttonView.getText().toString();
                if (text == null) return;
                if(isChecked) {
                    selectedPlayers.add(text);
                } else {
                    selectedPlayers.remove(text);
                }
                playerCountText.setText("" + selectedPlayers.size());
            }
        });
        return cb;
    }
}
