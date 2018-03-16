package uk.ac.abertay.forbes.assessment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 16/03/2018.
 */

public class ReadOptions extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_options);

        Intent intent = getIntent();

        ListView lv = findViewById(R.id.list_logs);

        // if (!databaseResponse) {
        dummyPopulate(lv);
        // }
    }

    public void dummyPopulate(ListView lv) {
        List<String> dummyValues = new ArrayList<String>();

        dummyValues.add("No Responses, A list");
        dummyValues.add("would look like this tho");

        for (int x = 1; x <= 10; x++) dummyValues.add("Item " + x);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, dummyValues);

        lv.setAdapter(arrayAdapter);
    }

}
