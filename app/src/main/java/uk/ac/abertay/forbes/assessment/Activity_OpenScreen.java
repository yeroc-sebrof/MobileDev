package uk.ac.abertay.forbes.assessment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Activity_OpenScreen extends AppCompatActivity {
    private static final String TAG = "Menu";

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
        Log.d(TAG, "Successful Launch");

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != 0))
        {
            final AlertDialog dialog = new AlertDialog.Builder(this, R.style.CptAppThemeDialog).create();
            dialog.setTitle(getText(R.string.app_name));
            dialog.setMessage("Hey, we notice you haven't given us permissions for Storage.\nIf you haven't made use of the app before and just want a look around that's cool but our app makes use of Storage for a whole bunch and kind of needs it.\nWe also for peak usage will make use of location access, SMS, and Call logging. But you can deny any of these you don't want to give us them.\nIf you're worried about any of the permissions we ask for and want to check it yourself have a look at our project on github.\n\nThanks for Downloading and we hope we meet your expectations");
            dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Let's deal with perms right now",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            requestPermissions(new String[]{Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, R.string.app_name);
                        }
                    });
            dialog.show();
        }
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
        Log.d(TAG, "New Log Pushed");
        startActivity(intent);
    }

    public void read(View view) {
        Intent intent = new Intent(this, Activity_ReadLogs.class);
        Log.d(TAG, "Review Log Pushed");
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