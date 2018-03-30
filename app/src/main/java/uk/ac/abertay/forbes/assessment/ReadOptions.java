package uk.ac.abertay.forbes.assessment;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

public class ReadOptions extends Activity {
    boolean deleting = Boolean.FALSE,
            dummyMode;

    Button delLogs;

    databaseHelper dh = new databaseHelper(this);
    SQLiteDatabase db;
    Cursor logs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_options);

        delLogs = findViewById(R.id.btn_delete_toggle);

        final Intent intent = getIntent();

        final ListView lv = findViewById(R.id.list_logs);

        db = this.openOrCreateDatabase(dh.getDatabaseName(), MODE_PRIVATE, null);

        try {
            logs = dh.readLogs(db);

            Log.d("Read Options", "Read Successful. Count " + logs.getCount());

            logs.moveToFirst();

            Log.d("Read Options", "Not Dummy Mode");
            populate(lv, logs);
        }
        catch (Exception e) {
            dh.onCreate(db);
            Log.d("Read Options", "Dummy Mode");
            Log.d("Read Options", e.toString());
            dummyPopulate(lv, -1);
            dummyMode = Boolean.TRUE;
        }

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = (String)(Object) lv.getItemAtPosition(position);
                if (deleting) {
                    Log.d("Read Options", "Log " + str + " Selected for Deletion");

                    // Update list
                    if (dummyMode == Boolean.TRUE) {
                        dummyPopulate(lv, position);
                    } else {
                        // Delete item in DB
                        logs.move(position);
                        dh.deleteLog(db, logs.getInt(0));

                        logs = dh.readLogs(db);
                        populate(lv, logs);
                    }

                    Toast.makeText(getApplicationContext(), str + " Deleted", Toast.LENGTH_LONG)
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
        Log.d("Read Options", "Successful Launch");
    }

    public void dummyPopulate(ListView lv, int repopulate) {
        Log.d("Read Options", "Dummy Populate Called");
        List<String> dummyValues = new ArrayList<String>();

        dummyValues.add("No Responses");
        dummyValues.add("List item Spam");

        for (int x = 1; x <= 10; x++) dummyValues.add("Item " + x);

        if (repopulate >= 0) {
            dummyValues.remove(repopulate);
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, dummyValues);

        lv.setAdapter(arrayAdapter);
    }

    public void populate(ListView lv, Cursor databaseResponse) {
        // this is also used as repopulate as all of the calls need to be remade to change our
        // dataset anyway so what's the point in keeping old data in memory
        Log.d("Read Options", "Populate Called");

        List<String> values = new ArrayList<String>();

        databaseResponse.moveToFirst();

        for (int x = databaseResponse.getCount(); x > 0; x--) {
            if (databaseResponse.getString(1) != null) {
                values.add("Log " + databaseResponse.getInt(0) + " (" + databaseResponse.getString(1) + ")");
            }
            else {
                values.add("Log " + databaseResponse.getInt(0));
            }

            Log.d("Read Options", x-1 + " items remain");
            databaseResponse.moveToNext();
        }

        databaseResponse.moveToFirst();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
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
        Intent intent = new Intent(this, ReadingLog.class);
        intent.putExtra("log", id);
        startActivity(intent);
    }

}
