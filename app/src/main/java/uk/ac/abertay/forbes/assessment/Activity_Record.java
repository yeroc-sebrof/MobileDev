package uk.ac.abertay.forbes.assessment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Calendar;

public class Activity_Record extends Activity {
    TextView time_end;
    EditText edit_text_log_name,
            edit_text_gps_time;

    String time = "04:00",
           log_name = "";

    Integer gps_time = 3, // 3 min between
            gps_maxtime = 9, // 9 min between
            timeSelHr = 4,
            timeSelMin = 0;

    Boolean debug,
            recording = false,
            gps,
            texts,
            calls;

    class BoolObj {
        public boolean value = false;

        void flip () {
            this.value = !this.value;
        }
    }

    public BoolObj power_saving = new BoolObj();

    ToggleButton gps_btn, text_btn, call_btn;

    // CountDownTimer timer;

    Service_Record record;

    View dirtyPassover; // This should only be used by the passover to the recording state when
                        // permissions did not exist. Must be a better way

    // TODO
    @Override
    public void onRequestPermissionsResult (int reqCode, String perms[], int[] results) {
        if (reqCode == R.string.app_name)
        {
            for (int x = perms.length; x > 0; x--)
            {
                if (results[x] == PackageManager.PERMISSION_DENIED)
                {
                    switch (perms[x])
                    {
                        case Manifest.permission.ACCESS_FINE_LOCATION:
                            gps = false;
                            break;
                        case Manifest.permission.READ_SMS:
                            texts = false;
                            break;
                        case Manifest.permission.READ_PHONE_STATE:
                            calls = false;
                            break;
                    }
                }
            }

            startRecording(dirtyPassover);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If the app Activity can be closed and reopened with the intent that the recording will continue.
//        if (savedInstanceState != null) {
//            if (savedInstanceState.getBoolean("Recording")) {
//                setContentView(R.layout.activity_recording_active);
//                recording = true;
//            }
//            else
//                setContentView(R.layout.activity_record_options);
//        }
//        else

        setContentView(R.layout.activity_record_options);

        time_end = findViewById(R.id.txt_end_at);
        time_end.setText(time);

        // https://developer.android.com/reference/android/support/v4/content/ContextCompat.html#checkSelfPermission(android.content.Context,%20java.lang.String)
        gps = (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == 0); // See current location
        texts = (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == 0); // Read incoming SMS
        calls = (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == 0); // See the incoming numbers

        // Error here is null as debug will always be passed
        debug = getIntent().getExtras().getBoolean("debug");

        Log.d("Record Options", "Successful Launch");
    }

    @Override
    public void finish() {
        // Do a check that the recording has ended
        record.stopSelf();

        super.finish();
    }

    // Below needs setup for opening the activity to the recording state if leaving the
    // recording activity is possible while continuing recording
    // https://stackoverflow.com/questions/14785806/android-how-to-make-an-activity-return-results-to-the-activity-which-calls-it
    // https://developer.android.com/training/basics/intents/result.html#ReceiveResult

//    @Override
//    public void onSaveInstanceState (Bundle savedInstanceState) {
//        super.onSaveInstanceState(savedInstanceState);
//
//
//        setResult(5);
//
//        if (recording)
//        {
//            savedInstanceState.putBoolean("Recording", true);
//            timer.cancel();
//        }
//    }

    public void endAt(View view) {
        final Calendar currentTimes = Calendar.getInstance();

        final int hour = currentTimes.get(Calendar.HOUR_OF_DAY);
        final int minute = currentTimes.get(Calendar.MINUTE);
        Log.d("Record Options","Current Time - " + hour + ":" + minute);

        TimePickerDialog timePicker =
                new TimePickerDialog(this, R.style.AppThemeDialog, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int sel_hour, int sel_minute) {
                        if (sel_hour < 10) {
                            time = "0" + sel_hour;
                        }
                        else {
                            time = "" + sel_hour;
                        }

                        if (sel_minute < 10) {
                            time += ":0" + sel_minute;
                        }
                        else {
                            time += ":" + sel_minute;
                        }

                        timeSelHr = sel_hour;
                        timeSelMin = sel_minute;
                        time_end.setText(time);
                        Log.i("Record Options", "Time Picker Done : " + time);
                        Log.d("Record Options", "Sel Hour: " + timeSelHr.toString() + ", Sel Min: " + timeSelMin.toString());

                        int toRecord = ((sel_hour * 60 + sel_minute) - (currentTimes.get(Calendar.HOUR_OF_DAY) * 60  + currentTimes.get(Calendar.MINUTE)) % 1440); // (Final - Current) mod (Min in day)
                        Log.i("Record Options", "Total Min to record: " + getString(toRecord));
                    }
                }, hour, minute, true);

        timePicker.show();
        Log.d("Record Options", "Time Picker Dialog displayed");
    }

    public void startRecording(View view) {
        final Calendar currentTimes = Calendar.getInstance();

        // Dont think Im gonna use this but ah well
        recording = true;

        // Start thread for tracking action
        // trackingService(time_end content)
        setContentView(R.layout.activity_recording_active);

        final TextView timeRemaining = findViewById(R.id.time_remaining);

        // Set to time of completion
        timeRemaining.setText(time);

        // Do the permissions checks here
        // If user does not allow for perms then just disable the features

        // Intent and Starting Service
        Intent intent = new Intent("recordpls");
        intent.putExtra("MinToLive", Integer.valueOf((timeSelHr * 60 + timeSelMin) - (currentTimes.get(Calendar.HOUR_OF_DAY) * 60  + currentTimes.get(Calendar.MINUTE)) % 1440));
        intent.putExtra("gps_time", gps_time);
        intent.putExtra("call", calls);
        intent.putExtra("sms", texts);
        intent.putExtra("gps", gps);

        record = new Service_Record(this, power_saving);
        record.onStartCommand(intent,0,R.string.app_name);

        // Timer I could use if I prefer
        // https://developer.android.com/reference/android/os/CountDownTimer.html
//        new CountDownTimer(30000, 1000) {
//            public void onTick(long millToCompletion) {
//                timeRemaining.setText(String.valueOf(millToCompletion/1000));
//            }
//
//            public void onFinish() {
//                timeRemaining.setText(R.string.end_at);
//            }
//        }.start();



    // Start recording service pseudo code
        // 1 tick the clock
        // 2 track the users location
            // 2.1 add points to log every x min
        // 3 listen for telephony
            // 3.1 this should be a listener for activities SMS and CALLS
        // 4 Make sure 1-3 correctly write to SQLite database as we run
            // 4.1 Make sure we write all Content correctly with JSON
            // https://www.w3schools.com/js/js_json_stringify.asp and
            // https://www.javacodegeeks.com/2013/10/android-json-tutorial-create-and-parse-json-data.html

//    public void wasRecording(View view) {
//        setContentView(R.layout.activity_recording_active);
    }

    public void callRecording(View view) {
        // Ask the user for permissions if they are lacking
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_SMS) != 0 ||
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != 0 ||
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != 0) {
            requestPermissions(new String[]{Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_FINE_LOCATION}, R.string.app_name);
            dirtyPassover = view;
        }
        else startRecording(view);
    }

    public void stopRecording(View view) {
        // Stop the service from running
        record.stopSelf();

        // Back off this Activity
        this.finish();
    }

    public void moreOptions(View view) {
        if (debug) {
            debugMakeLogs(view);
        }
        else
        {
            // Make a dialog with more_options_dialog.xml
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppThemeDialog);

            builder.setTitle(R.string.more_options)
                    .setView(R.layout.more_options_dialog)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            gps = gps_btn.isChecked();
                            texts = text_btn.isChecked();
                            calls = call_btn.isChecked();

                            if (Integer.parseInt(edit_text_gps_time.getText().toString()) > gps_maxtime)
                                gps_time = gps_maxtime;
                            else
                                gps_time = Integer.parseInt(edit_text_gps_time.getText().toString());

                            log_name = edit_text_log_name.getText().toString();
                        }
                    });

            AlertDialog dialog = builder.create();

            dialog.show();

            // https://stackoverflow.com/questions/37934882/onrequestpermissionsresultcallback-not-triggering-in-preferencefragment
            // https://developer.android.com/reference/android/support/v4/app/ActivityCompat.html#requestPermissions(android.app.Activity,%20java.lang.String[],%20int)
            text_btn = dialog.findViewById(R.id.btn_Texts);
            Log.i("Record Options", "Texts is " + texts.toString());
            text_btn.setChecked(texts);

            call_btn = dialog.findViewById(R.id.btn_Calls);
            Log.i("Record Options", "Calls is " + calls.toString());
            call_btn.setChecked(calls);

            gps_btn = dialog.findViewById(R.id.btn_GPS);
            Log.i("Record Options", "Locations is " + gps.toString());
            gps_btn.setChecked(gps);

            edit_text_log_name = dialog.findViewById(R.id.editLogname);
            edit_text_log_name.setText(log_name);

            edit_text_gps_time = dialog.findViewById(R.id.editLocationPingTime);
            edit_text_gps_time.setText(String.valueOf(gps_time));
        }
    }

    public void powerSavings(View view) {
        int temp;

        if (debug) {
            debugMakeItemInLog(view);
        }
        else {
            // Change between the users time and the power saving time
            temp = gps_maxtime;
            gps_maxtime = gps_time;
            gps_time = temp;
            power_saving.flip();
            Toast.makeText(getApplicationContext(),"Power Saving " + power_saving.value, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void debugMakeItemInLog(View view) {
        Log.d("Record Options", "Debug Make log action Called");

        AsyncDatabaseHelper debugHelp = new AsyncDatabaseHelper(this);
        SQLiteDatabase debugDatabase = this.openOrCreateDatabase(debugHelp.DATABASE_NAME,
                MODE_PRIVATE, null);
        Log.d("Record Options", "Database connection established");

        debugHelp.debugAddActions(debugDatabase);

        Log.d("Record Options", "Debug log items added");
    }

    public void debugMakeLogs(View view) {
        Log.d("Record Options", "Debug Make logs Called");

        AsyncDatabaseHelper debugHelp = new AsyncDatabaseHelper(this);
        SQLiteDatabase debugDatabase = this.openOrCreateDatabase(debugHelp.DATABASE_NAME,
                                                                        MODE_PRIVATE,null);
        Log.d("Record Options", "Database connection established");

        debugHelp.makeNewLog(debugDatabase, "fake news");
        Log.d("Record Options", "Debug logs made");
    }
}
