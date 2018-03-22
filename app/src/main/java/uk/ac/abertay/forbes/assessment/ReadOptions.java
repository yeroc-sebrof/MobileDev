package uk.ac.abertay.forbes.assessment;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 16/03/2018.
 */

public class ReadOptions extends Activity {
    boolean deleting = Boolean.FALSE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_options);

        final Intent intent = getIntent();
        final ListView lv = findViewById(R.id.list_logs);

        // Check for a database containing a list of the databases.

        // if (there is not a databases containing a log) {
        dummyPopulate(lv);
        // }
        // else { populate the list with the logs that exist }

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String str = (String)(Object) lv.getItemAtPosition(position);
                if (deleting)
                    { Toast.makeText(getApplicationContext(),
                        "Deleting: " + str,
                        Toast.LENGTH_SHORT).show();
                        // delete current item clicked
                        //check if this updates the list upon completetion or if the page must refresh
                    }
                else
                    { Toast.makeText(getApplicationContext(),
                        str,
                        Toast.LENGTH_SHORT).show();
                        openLog(view, str);
                    }
            }
        });
    }

    public void dummyPopulate(ListView lv) {
        List<String> dummyValues = new ArrayList<String>();

        dummyValues.add("No Responses");
        dummyValues.add("List item Spam");

        for (int x = 1; x <= 10; x++) dummyValues.add("Item " + x);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, dummyValues);

        lv.setAdapter(arrayAdapter);
    }

    public void delLog(View view) {
        if (deleting == Boolean.TRUE) {deleting = Boolean.FALSE;} else {deleting = Boolean.TRUE;}
    }

    public void openLog(View view, String str) {
        Intent intent = new Intent(this, ReadingLog.class);
        startActivity(intent);
    }

}
