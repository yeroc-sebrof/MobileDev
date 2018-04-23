package uk.ac.abertay.forbes.assessment;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Activity_OpenScreen extends AppCompatActivity {
    int debug = 0;
    boolean debugActive = false,
            currentlyRecording = false;

    Button btn_newLog;

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
        btn_newLog = findViewById(R.id.btn_new_log);
        Log.d("Main Menu", "Successful Launch");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 5 && requestCode == 0) {
            btn_newLog.setText(R.string.recording_status);
        }
        else
        {
            btn_newLog.setText(R.string.new_log);
        }
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
        startActivityForResult(intent, 0);
    }

    public void debugPoke (View view) {
        debug++;
        if (debug > 5) {
            debugActive = !debugActive;
            Toast.makeText(getApplicationContext(),"Debug gestures " + debugActive, Toast.LENGTH_LONG)
                    .show();
        }
    }
}