package uk.ac.abertay.forbes.assessment;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

public class Record extends Activity {
    final TextView time_end = (TextView)findViewById(R.id.txt_end_at);
    final String time_default = "04:00";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_options);

        time_end.setText(time_default);
        Intent intent = getIntent();
    }

    void endTime() {
        Calendar currentTimes = Calendar.getInstance();
        final int hour = currentTimes.get(Calendar.HOUR_OF_DAY);
        final int minute = currentTimes.get(Calendar.MINUTE);

        TimePickerDialog timePicker =
                new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int sel_hour, int sel_minute) {
                        time_end.setText(sel_hour + ":" + sel_minute); // TODO alternative
                    }
                }, hour, minute, false);
        timePicker.setTitle("End Time");
        timePicker.show();
    }

    public void startRecording(View view) {
        // Start thread for tracking action
        // startTracking(time_start content, time_end content)

        setContentView(R.layout.activity_recording_active);
    }

    /*
    public void startTracking(View view) {
        // tick the clock
        // track the users location
            // add points to log
        // track telephony
            // this should be a listener for activities (Ask Johnny about this from calls)

        // Make sure the SQLite database is being added to as this goes and not left as one big data dump
    }
    */

    public void stopRecording(View view) {
        setContentView(R.layout.activity_record_options);

        // Reset time end
        time_end.setText(time_default);
    }
}
