package uk.ac.abertay.forbes.assessment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by admin on 16/03/2018.
 */

public class ReadingLog extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_log);

        Intent intent = getIntent();
    }
}
