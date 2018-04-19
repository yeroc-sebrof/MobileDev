package uk.ac.abertay.forbes.assessment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import java.util.Calendar;

public class Activity_Record extends Activity {
    TextView time_end;
    EditText edit_text_log_name,
            edit_text_gps_time;

    String time = "04:00",
           log_name = null;
    Integer gps_time;
    Boolean debug,
            gps = true, // TODO FIX THIS CRASHING THE MORE OPTION MENUE
            texts = true,
            calls = true;

    ToggleButton gps_btn, text_btn, call_btn;// TODO ABOVE TO DO WITH THIS TOO


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_options);

        time_end = findViewById(R.id.txt_end_at);
        time_end.setText(time);

        // Error here is null as debug will always be passed
        debug = getIntent().getExtras().getBoolean("debug");

        Log.d("Activity_Record Options", "Successful Launch");
    }

    public void endAt(View view) {
        Calendar currentTimes = Calendar.getInstance();

        final int hour = currentTimes.get(Calendar.HOUR_OF_DAY);
        final int minute = currentTimes.get(Calendar.MINUTE);
        Log.d("Activity_Record Options", "Current Time - " + hour + ":" + minute);

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

                        time_end.setText(time);
                        Log.d("Activity_Record Options", "Time Picker Done : " + time);
                    }
                }, hour, minute, true);

        timePicker.show();
        Log.d("Activity_Record Options", "Time Picker Dialog displayed");
    }

    public void startRecording(View view) {
        TextView timeRemianing = findViewById(R.id.time_remaining);

        // Start thread for tracking action
        // trackingService(time_end content)

        // tick the clock

        setContentView(R.layout.activity_recording_active);

    }

    private void trackingService () {

    }

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

    public void stopRecording(View view) {
        // Stop the service from running

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
                    .setNegativeButton("Dinnie", new DialogInterface.OnClickListener() { // Might not need a negative button
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Nothing needs done here tbh
                        }
                    })
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Save the state of all of the options
                        }
                    });

            AlertDialog dialog = builder.create();

            gps_btn = findViewById(R.id.btn_GPS); // TODO ABOVE PROBLEM LIES HERE IN EXECUTION
            gps_btn.setChecked(gps); // TODO I THINK ITS DUE TO THE DIALOG NOT LOADING IN YET?

            text_btn = findViewById(R.id.btn_Texts);
            text_btn.setChecked(texts);

            call_btn = findViewById(R.id.btn_Calls);
            call_btn.setChecked(calls);

            edit_text_gps_time = findViewById(R.id.editLocationPingTime);
            edit_text_gps_time.setText(gps_time);

            edit_text_log_name = findViewById(R.id.editLogname);
            edit_text_log_name.setText(log_name);

            dialog.show();
        }
    }

    public void powerSavings(View view) {
        if (debug) {
            debugMakeItemInLog(view);
        }
        else{
            // Change the time between GPS pings to 5 or 6 min
        }
    }

    public void debugMakeItemInLog(View view) {
        Log.d("Activity_Record Options", "Debug Make log action Called");

        AsyncDatabaseHelper debugHelp = new AsyncDatabaseHelper(this);
        SQLiteDatabase debugDatabase = this.openOrCreateDatabase(debugHelp.getDatabaseName(),
                MODE_PRIVATE, null);
        Log.d("Activity_Record Options", "Database connection established");

        debugHelp.lastItemInLogIndex(debugDatabase);

        Log.d("Activity_Record Options", "Debug logs made");
    }

    public void debugMakeLogs(View view) {
        Log.d("Activity_Record Options", "Debug Make logs Called");

        AsyncDatabaseHelper debugHelp = new AsyncDatabaseHelper(this);
        SQLiteDatabase debugDatabase = this.openOrCreateDatabase(debugHelp.getDatabaseName(),
                                                                        MODE_PRIVATE, null);
        Log.d("Activity_Record Options", "Database connection established");

        debugHelp.makeNewLog(debugDatabase, "fake news");
        Log.d("Activity_Record Options", "Debug logs made");
    }
}
