package uk.ac.abertay.forbes.assessment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

// TODO Make the list use subtitles instead https://youtu.be/VYDLTBjdliY

public class Activity_ReadLogs extends Activity {
    boolean deleting = Boolean.FALSE,
            dummyMode;

    Button delLogs;

    AsyncDatabaseHelper dh;
    SQLiteDatabase db;

    ArrayAdapter<String> arrayAdapter;
    ListView lv;

    Cursor logs;

    public void onRequestPermissionsResult (int reqCode, @NonNull String perms[], @NonNull int[] results) {
        if (reqCode == R.string.app_name) {
            for (int x = perms.length; x > 0; ) {
                x--;
                if (results[x] == PackageManager.PERMISSION_DENIED) {
                    switch (perms[x]) {
                        case Manifest.permission.READ_EXTERNAL_STORAGE:
                            Toast.makeText(getApplicationContext(), "This application cannot run without Storage Permissions", Toast.LENGTH_LONG)
                                    .show();
                            this.finish();
                            break;
                    }
                }
                else
                {
                    db = getApplication().openOrCreateDatabase(AsyncDatabaseHelper.DATABASE_NAME, MODE_PRIVATE, null);
                    // ON CREATE SHOULD CALL HERE!!!
                    dh = new AsyncDatabaseHelper(this);
                    // BUT IT STILL HAS TO BE CALLED HERE
                    dh.onCreate(db);
                    dh.readLogs(db, this);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_options);

        delLogs = findViewById(R.id.btn_delete_toggle);
        lv = findViewById(R.id.list_logs);

        Boolean READ = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == 0;

        Log.d("Read Options", "Read permissions " + READ.toString());
        if (READ) {
            db = getApplication().openOrCreateDatabase(AsyncDatabaseHelper.DATABASE_NAME, MODE_PRIVATE, null);
            dh = new AsyncDatabaseHelper(this); // ON CREATE SHOULD CALL HERE
            dh.onCreate(db);
            dh.readLogs(db, this);
        }
        else
        {
            requestPermissions(new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, R.string.app_name);
        }

        Log.d("Read Options", "Successful Launch");
    }

    public void startList(Cursor cursor) {
        try {
            logs = cursor;

            Log.d("Read Options", "Read Successful. Count " + logs.getCount());

            logs.moveToFirst();

            Log.d("Read Options", "Not Dummy Mode");
            populate();
        }
        catch (Exception e) {
            dh.onCreate(db);
            Log.d("Read Options", "Dummy Mode");
            Log.d("Read Options", e.toString());
            dummyPopulate(-1);
            dummyMode = Boolean.TRUE;
        }

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = (String)(Object) lv.getItemAtPosition(position);
                if (deleting) {
                    Log.d("Read Options", "Log " + str + " Selected for Deletion");

                    // Update list
                    if (dummyMode == Boolean.TRUE) {
                        dummyPopulate(position);
                    } else {
                        // Delete item in DB
                        logs.move(position);
                        dh.deleteLog(db, logs.getInt(0));
                        arrayAdapter.remove(str);
                        dh.resetLogs(db);
                    }

                    Toast.makeText(getApplicationContext(), str + " Deleted", Toast.LENGTH_SHORT)
                         .show();

                    delLogToggle(view);
                }
                else {
                    Log.d("Read Options", position + " Selected");

                    if (dummyMode != Boolean.TRUE) {
                        logs.move(position);
                        openLog(view, logs.getInt(0));
                        logs.moveToFirst();
                    }
                }
            }
        });
    }

    public void dummyPopulate(int repopulate) {
        Log.d("Read Options", "Dummy Populate Called");
        List<String> dummyValues = new ArrayList<>();

        dummyValues.add("No Responses");
        dummyValues.add("List item Spam");

        for (int x = 1; x <= 10; x++) dummyValues.add("Item " + x);

        if (repopulate >= 0) {
            dummyValues.remove(repopulate);
        }

        arrayAdapter = new ArrayAdapter<> (this,
                android.R.layout.simple_list_item_1,
                dummyValues);

        lv.setAdapter(arrayAdapter);
    }

    public void populate() {
        // this is also used as repopulate as all of the calls need to be remade to change our
        // dataset anyway so what's the point in keeping old data in memory
        Log.d("Read Options", "Populate Called");

        List<String> values = new ArrayList<>();

        logs.moveToFirst();

        for (int x = logs.getCount(); x > 0; x--) {
            if (logs.getString(1) != null) {
                values.add("Log " + logs.getInt(0) + " (" + logs.getString(1) + ")");
            }
            else {
                values.add("Log " + logs.getInt(0));
            }

            Log.d("Read Options", x-1 + " items remain");
            logs.moveToNext();
        }

        logs.moveToFirst();

        arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, values);

       lv.setAdapter(arrayAdapter);
    }

    public void delLogToggle(View view) {
        if (deleting == Boolean.TRUE) {
                deleting = Boolean.FALSE;
                delLogs.setText(R.string.delete_log);
            }
        else {
                deleting = Boolean.TRUE;
                delLogs.setText(R.string.cancel_delete_logs);
            }


        Log.d("Read Options", "Deleting mode: " + deleting);
    }

    public void openLog(View view, int id) {
        Intent intent = new Intent(this, Activity_ReadingLog.class);
        intent.putExtra("log", id);
        startActivity(intent);
    }
}
