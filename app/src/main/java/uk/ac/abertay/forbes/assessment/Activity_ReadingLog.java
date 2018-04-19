package uk.ac.abertay.forbes.assessment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class Activity_ReadingLog extends Activity {
    int log;

    TextView readingLog;

    AsyncDatabaseHelper dh = new AsyncDatabaseHelper(this);
    SQLiteDatabase db;

    ArrayAdapter<String> arrayAdapter;
    ListView logV;

    Cursor logCurr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_log);

        logV = findViewById(R.id.log_view);
        readingLog = findViewById(R.id.txt_currentActivity);

        try {
            log = getIntent().getExtras().getInt("log");
            Log.d("Read Log", "Reading " + log);

            readingLog.append(" Log " + log);
        }
        catch (Exception e) {
            Log.e("Read Log", "Intent failed to include an Int - " + e.toString());
            this.finish();
        }

        try {
            db = this.openOrCreateDatabase(dh.getDatabaseName(), MODE_PRIVATE, null);
        }
        catch (Exception e) {
            Log.e("Read Log", "How did you get this far and couldn't open a database - " + e.toString());
            Toast.makeText(getApplicationContext(), "Unexpected error when opening a Database", Toast.LENGTH_LONG)
                    .show();
            this.finish();
        }

        dh.readLog(db, log,this);

        Log.d("Read Log", "Successful Launch");
    }

    public void startList(Cursor cursor) {
        logCurr = cursor;

        logCurr.moveToNext();

        Log.d("Read Log", "Log output has " + logCurr.getCount() + " items");

        List<String> logViewValues = new ArrayList<String>();

        for (int x = logCurr.getCount(); x > 0; x--) {
            logViewValues.add(dh.TYPES[logCurr.getInt(0)] + parseJson());
            Log.d("Read Log", x-1 + " items remain");
            logCurr.moveToNext();
        }

        logCurr.moveToFirst();

        arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, logViewValues);

        logV.setAdapter(arrayAdapter);

        logV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                takeItemFurther(view, position);
                return true;
            }
        });

        Log.d("Read Log", "Successful Database pull");
    }

    String parseJson () {
        JSONObject JSONFunctionParser;

        try {
            JSONFunctionParser = new JSONObject(logCurr.getString(1));
        }
        catch (Exception e){
            Log.d("Read Log", "Parser Broke - " + e.toString());
            return " this item has been corrupted";
        }

        try {
            Log.d("Read Log", "Parsing JSON " + dh.getTYPES()[logCurr.getInt(0)] );
        }
        catch (Exception e){
            Log.d("Read Log", "Parsing JSON " + logCurr.getInt(0));
            Log.e("Read Log", "Parse JSON error " + e.toString());
        }

        if (logCurr.getInt(0) == 0) { // We dont need to parse extra information for this
            return " Ping";
        }

        if (logCurr.getInt(0) == 1 || logCurr.getInt(0) == 2) {
            try {
                if (JSONFunctionParser.getBoolean("outbound")) {
                    return " outbound to " + JSONFunctionParser.getString("contact");
                } else {
                    return " inbound from " + JSONFunctionParser.getString("contact");
                }
            } catch (Exception e) {
                Log.d("Read Log", "Parser Broke - " + e.toString());
                return " this item has been corrupted";
            }
        }

        Log.d("Read Log", logCurr.getInt(0) + " does not have a relevant return - " + logCurr.getString(1));
        return " this item has been corrupted";
    }


    void takeItemFurther(View view, int item) {
        // This one should be used on long push of item in log
        Log.d("Read Log", "Long Clicked item " + item);

        JSONObject JSONParser;

        logCurr.move(item);
        String content = "";

        try {

            JSONParser = new JSONObject(logCurr.getString(1));

            switch (logCurr.getInt(0)) {
                case 0:
                    try {
                        content = "Lat - " + JSONParser.getString("lat-float") +
                                "\nLong - " + JSONParser.getString("long-float");
                    }
                    catch (Exception e) {
                        Log.d("Read Log", "Lat/Long didnt work");
                        content = "There was an error.\nSorry!  Err - 1";
                    }
                    break;
                case 1:
                    try {
                        if (JSONParser.getBoolean("outbound")) {
                            content += "To - ";
                        }
                        else {
                            content += "From - ";
                        }

                        content += JSONParser.getString("contact") + "\n";

                        content+="\nCall Start - " + JSONParser.getString("start") +
                              "\nCall End - " + JSONParser.getString("end");
                    }
                    catch (Exception e) {
                        // This ones the most likely fuck up
                        Log.d("Read Log", "Contact/Outbound/Start/End didn't work");
                        content = "There was an error.\nSorry!  Err - 2";
                    }
                    break;
                case 2:
                    try {
                        if (JSONParser.getBoolean("outbound")) {
                            content += "To - ";
                        }
                        else {
                            content += "From - ";
                        }

                        content += JSONParser.getString("contact") + "\n";

                        content+="\nContent:\n\n" + JSONParser.getString("content");
                    }
                    catch (Exception e) {
                        Log.d("Read Log", "Contact/Outbound/Content didn't work");
                        content = "There was an error.\nSorry!  Err - 3";
                    }
                    break;
            }

            content += "\n\n" + "Timestamp: " + logCurr.getString(2);
        }
        catch (Exception e) {
            Log.d("Read Log", "Reading JSON failed - " + e.toString());
            content = "There was an error.\nSorry!  Err - 4";
        }

        final AlertDialog dialog = new AlertDialog.Builder(this, R.style.AppThemeDialog).create();
        dialog.setTitle(dh.TYPES[logCurr.getInt(0)]);
        dialog.setMessage(content);
        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cool Beans!",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });
        dialog.show();

        logCurr.moveToFirst();
        Log.d("Read Log", "Long Click done");

    }

}
