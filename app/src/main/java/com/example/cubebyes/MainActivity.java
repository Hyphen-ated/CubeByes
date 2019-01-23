package com.example.cubebyes;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton startCube = findViewById(R.id.startCubeButton);
        startCube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               createLotsOfBoxes();
            }
        });

        FloatingActionButton refresh = findViewById(R.id.refreshButton);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "dingus malingus", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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

    private void createLotsOfBoxes() {
        LinearLayout playerList = findViewById(R.id.playerList);
        playerList.removeAllViews();
        for (int i = 0; i < 30; ++i) {
            CheckBox cb = new CheckBox(getApplicationContext());
            cb.setText("Player number " + (i + 1));
            cb.setTextSize(32);
            cb.setTextColor(Color.DKGRAY);
            cb.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            playerList.addView(cb);
        }
    }
}
