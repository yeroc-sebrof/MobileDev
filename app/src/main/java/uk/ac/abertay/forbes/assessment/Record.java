package uk.ac.abertay.forbes.assessment;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class Record extends Activity {
    TextView time_end;
    String time = "04:00";
    Boolean debug;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_options);

        time_end = findViewById(R.id.txt_end_at);
        time_end.setText(time);

        // Error here is null as debug will always be passed
        debug = getIntent().getExtras().getBoolean("debug");

        Log.d("Record Options", "Successful Launch");
    }

    public void endAt(View view) {
        Calendar currentTimes = Calendar.getInstance();

        final int hour = currentTimes.get(Calendar.HOUR_OF_DAY);
        final int minute = currentTimes.get(Calendar.MINUTE);
        Log.d("Record Options", "Current Time - " + hour + ":" + minute);

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
                        Log.d("Record Options", "Time Picker Done : " + time);
                    }
                }, hour, minute, true);

        timePicker.show();
        Log.d("Record Options", "Time Picker Dialog displayed");
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

    public void debugMakeItemInLog(View view) {
        Log.d("Record Options", "Debug Make log action Called");

        asyncDatabaseHelper debugHelp = new asyncDatabaseHelper(this);
        SQLiteDatabase debugDatabase = this.openOrCreateDatabase(debugHelp.getDatabaseName(),
                MODE_PRIVATE, null);
        Log.d("Record Options", "Database connection established");

        Cursor temp = debugHelp.lastItemInLogIndex(debugDatabase);
        temp.moveToNext();

        debugHelp.newActivity(debugDatabase, temp.getInt(0), 1,
                "{\n\t\"contact\":\"The Captain\",\n\t\"outbound\":false,\n\t\"start\":\"Alpha\",\n\t\"end\":\"Omega\"\n}");

        debugHelp.newActivity(debugDatabase, temp.getInt(0), 2,
                "{\n\t\"contact\":\"The Captain\",\n\t\"outbound\":false,\n\t\"content\":\"Get a bottle of Morgans on the way back\"\n}");

        debugHelp.newActivity(debugDatabase, temp.getInt(0), 2,
                "{\n\t\"contact\":\"The Captain\",\n\t\"outbound\":true,\n\t\"content\":\"Of Course!\"\n}");

        Log.d("Record Options", "Debug logs made");
    }

    public void debugMakeLogs(View view) {
        Log.d("Record Options", "Debug Make logs Called");

        asyncDatabaseHelper debugHelp = new asyncDatabaseHelper(this);
        SQLiteDatabase debugDatabase = this.openOrCreateDatabase(debugHelp.getDatabaseName(),
                                                                        MODE_PRIVATE, null);
        Log.d("Record Options", "Database connection established");

        debugHelp.makeNewLog(debugDatabase, "fake news");
        Log.d("Record Options", "Debug logs made");
    }
}
