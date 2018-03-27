package uk.ac.abertay.forbes.assessment;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class Record extends Activity {
    TextView time_end;
    final String time_default = "04:00";
    String time = "04:00";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_options);

        time_end = findViewById(R.id.txt_end_at);
        time_end.setText(time_default);
        Log.d("Record Options", "Set Time to " + time_default);

        Intent intent = getIntent();

        Log.d("Record Options", "Successful Launch");
    }

    public void endTime(View view) {
        Calendar currentTimes = Calendar.getInstance();

        final int hour = currentTimes.get(Calendar.HOUR_OF_DAY);
        final int minute = currentTimes.get(Calendar.MINUTE);
        Log.d("Record Options", "Variables set");

        TimePickerDialog timePicker =
                new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int sel_hour, int sel_minute) {
                        if (sel_minute < 10) {
                            time = sel_hour + ":0" + sel_minute;
                        }
                        else {
                            time = sel_hour + ":" + sel_minute;
                        }
                        time_end.setText(time);
                        Log.d("Record Options", "Time Picker Done : " + time);
                    }
                }, hour, minute, true);

        timePicker.setTitle("End Time");

        timePicker.show();
        Log.d("Record Options", "Time Picker Dialog displayed");
    }

    public void startRecording(View view) {
        // Start thread for tracking action
        // startTracking(time_end content)

        setContentView(R.layout.activity_recording_active);
    }


    public void startTracking(View view) {
        // tick the clock
        // track the users location
            // add points to log
        // track telephony
            // this should be a listener for activities

        // Make sure the SQLite database is being added to as this goes and not left as one big data dump
    }

    public void stopRecording(View view) {
        setContentView(R.layout.activity_record_options);

        // Back off this Activity

    }
}
