package uk.ac.abertay.forbes.assessment;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


public class ReadingLog extends Activity {
    TextView readingLog;
    int log;

    databaseHelper dh = new databaseHelper(this);
    SQLiteDatabase db;
    Cursor logOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_log);
        readingLog = findViewById(R.id.txt_currentActivity);

        try {
            log = getIntent().getExtras().getInt("log");
            Log.d("Read Log", "Reading " + log);

            readingLog.append(" Log " + log);
        }
        catch (Exception e) {
            Log.e("Read Log", "getInt failed - " + e.toString());
            this.finish();
        }

        try {
            db = this.openOrCreateDatabase(dh.getDatabaseName(), MODE_PRIVATE, null);
        }
        catch (Exception e) {
            Log.e("Read Log", "Unable to read log - " + e.toString());
            this.finish();
        }

        logOut = dh.readLog(db, log);


        Log.d("Read Log", "Successful Launch");
    }
}
