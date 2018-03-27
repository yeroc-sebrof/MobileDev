package uk.ac.abertay.forbes.assessment;

import android.app.Activity;
import android.content.Intent;
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

public class ReadOptions extends Activity {
    boolean deleting = Boolean.FALSE;
    Button delLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_options);

        delLogs = findViewById(R.id.btn_delete_toggle);

        final Intent intent = getIntent();
        final ListView lv = findViewById(R.id.list_logs);

        // Start using databases
        //databaseHelper dh = new databaseHelper(this);
        //SQLiteDatabase db = this.openOrCreateDatabase(dh.getDatabaseName(), MODE_PRIVATE, null);

        //dh.readLogs(db);

        // if (there is not a databases containing a log) {
        dummyPopulate(lv);
        // }
        // else { populate(lv); }

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = (String)(Object) lv.getItemAtPosition(position);
                if (deleting)
                    {
                        Log.d("Read Options", "Log " + str + " Selected for Deletion");

                        // delete current item clicked
                        // Update list

                        Toast.makeText(getApplicationContext(),
                        "Log " + str + " Deleted",
                        Toast.LENGTH_LONG).show();
                        delLog(view);
                    }
                else
                    {
                        Log.d("Read Options", "Log '" + str + "' Selected");
                        //openLog(view, str);
                    }
            }
        });
        Log.d("Read Options", "Successful Launch");
    }

    public void dummyPopulate(ListView lv) {
        Log.d("Read Options", "Dummy Populate Called");
        List<String> dummyValues = new ArrayList<String>();

        dummyValues.add("No Responses");
        dummyValues.add("List item Spam");

        for (int x = 1; x <= 10; x++) dummyValues.add("Item " + x);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, dummyValues);

        lv.setAdapter(arrayAdapter);
    }

    public void populate(ListView lv) {
        // TODO
    }

    public void delLog(View view) {
        if (deleting == Boolean.TRUE)
            {
                deleting = Boolean.FALSE;
                delLogs.setText(R.string.delete_log);
            }
        else
            {
                deleting = Boolean.TRUE;
                delLogs.setText(R.string.cancel_delete_logs);
            }


        Log.d("Read Options", "Deleting mode: " + deleting);
    }

    public void openLog(View view, String str) {
        Intent intent = new Intent(this, ReadingLog.class);
        startActivity(intent);
    }

}
