package com.example.cubebyes;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
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

    public static final String playerFileName = "players.txt";
    public static final String githubUrl = "https://raw.githubusercontent.com/Hyphen-ated/CubeByeAffinities/master/players.txt";

    
    private void snackMsg(String msg) {
        Snackbar bar = Snackbar.make(playerCountText, msg, 2000);
        bar.show();
    }
    
    private void snackMsg(String msg, Throwable tr) {
        snackMsg(msg);
        Log.e("cubebyes", msg, tr);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //we are running network on the main thread because there is nothing else the app can ever do while waiting
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

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
                setupPlayersFromFile();
            }
        });

        playerCountText = findViewById(R.id.playerCount);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updatePlayerData() {
        URI uri = null;
        try {
            uri = new URI(githubUrl);
        } catch (URISyntaxException e) {
            snackMsg("Bad URL for player data", e);
            return;
        }
        String data = null;
        try {
            data = IOUtils.toString(uri, "utf-8");
        } catch (IOException e) {
            snackMsg("Couldn't get player data from the internet", e);
            return;
        }
        File dir = getFilesDir();
        File playersFile = new File(dir, playerFileName);
        try {
            IOUtils.write(data, new FileOutputStream(playersFile));
        } catch (IOException e) {
            snackMsg("Couldn't write player data to a file", e);
            return;
        }
        snackMsg("Updated player data from the internet");
    }

    private void setupPlayersFromFile() {
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
            snackMsg("Failed to read local players file", e);
            return;
        }


        Pattern pat = Pattern.compile("^\\s*(\\d*)\\s+(.+)");
        LinearLayout playerList = findViewById(R.id.playerList);
        playerList.removeAllViews();
        for (String s : lines) {
            Matcher m = pat.matcher(s);
            if(m.matches()) {
                int affinity = Integer.parseInt(m.group(1));
                String name = m.group(2);
                playerAffinities.put(name, affinity);
            }

        }


        populateList();
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
