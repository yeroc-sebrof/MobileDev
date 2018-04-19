package uk.ac.abertay.forbes.assessment;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class Activity_OpenScreen extends AppCompatActivity {
    int debug = 0;
    boolean debugActive = Boolean.FALSE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* try {
            setContentView(R.layout.activity_open_screen);
            wait(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } */

        setContentView(R.layout.activity_main_menu);
        Log.d("Main Menu", "Successful Launch");
    }

    public void make(View view) {
        Intent intent = new Intent(this, Activity_Record.class);
        intent.putExtra("debug", debugActive);
        Log.d("Main Menu", "New Log Pushed");
        startActivity(intent);
    }

    public void read(View view) {
        Intent intent = new Intent(this, Activity_ReadLogs.class);
        Log.d("Main Menu", "Review Log Pushed");
        startActivity(intent);
    }

    public void debugPoke (View view) {
        debug++;
        if (debug > 10) {
            Toast.makeText(getApplicationContext(),"Debug gestures are active", Toast.LENGTH_LONG)
                    .show();
            debugActive = Boolean.TRUE;
        }
    }
}