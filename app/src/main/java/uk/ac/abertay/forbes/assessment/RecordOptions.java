package uk.ac.abertay.forbes.assessment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by admin on 16/03/2018.
 */

public class RecordOptions extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_options);

        Intent intent = getIntent();
    }

    public void makeStart(View view) {
        Intent intent = new Intent(this, RecordingActive.class);
        startActivity(intent);
    }
}
