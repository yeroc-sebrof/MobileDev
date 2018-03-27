package uk.ac.abertay.forbes.assessment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


public class ReadingLog extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_log);

        Intent intent = getIntent();

        Log.d("Read Log", "Successful Launch");
    }
}
