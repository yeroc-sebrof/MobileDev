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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ReadingLog extends Activity {
    TextView readingLog;
    ListView logView;
    int log;

    databaseHelper dh = new databaseHelper(this);
    SQLiteDatabase db;
    Cursor logOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_log);

        List<String> logViewValues = new ArrayList<String>();

        readingLog = findViewById(R.id.txt_currentActivity);
        logView = findViewById(R.id.log_view);

        logView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                takeItemFurther(view, position);
                return true;
            }
        });

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
        logOut.moveToNext();

        Log.d("Read Log", "Log output has " + logOut.getCount() + " items");

        for (int x = logOut.getCount(); x > 0; x--) {
                logViewValues.add(dh.TYPES[logOut.getInt(0)] + parseJson());
                Log.d("Read Log", x-1 + " items remain");
                logOut.moveToNext();
        }

        logOut.moveToFirst();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, logViewValues);

        logView.setAdapter(arrayAdapter);

        Log.d("Read Log", "Successful Launch");
    }

    String parseJson () {
        JSONObject JSONFunctionParser;

        try {
            JSONFunctionParser = new JSONObject(logOut.getString(1));
        }
        catch (Exception e){
            Log.d("Read Log", "Parser Broke - " + e.toString());
            return " this item has been corrupted";
        }

        try {
            Log.d("Read Log", "Parsing JSON " + dh.TYPES[logOut.getInt(0)]);
        }
        catch (Exception e){
            Log.d("Read Log", "Parsing JSON " + logOut.getInt(0));
            Log.e("Read Log", "Parse JSON error " + e.toString());
        }

        if (logOut.getInt(0) == 0) { // We dont need to parse extra information for this
            return " Ping";
        }

        if (logOut.getInt(0) == 1 || logOut.getInt(0) == 2) {
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

        Log.d("Read Log", logOut.getInt(0) + " does not have a relevant return - " + logOut.getString(1));
        return " this item has been corrupted";
    }


    void takeItemFurther(View view, int item) {
        // This one should be used on long push of item in log
        Log.d("Read Log", "Long Clicked item " + item);

        JSONObject JSONParser;

        logOut.move(item);
        String temp = "";

        try {

            JSONParser = new JSONObject(logOut.getString(1));

            switch (logOut.getInt(0)) {
                case 0:
                    try {
                        temp = "Lat - " + JSONParser.getString("lat-float") +
                                "\nLong - " + JSONParser.getString("long-float");
                    }
                    catch (Exception e) {
                        Log.d("Read Log", "Lat/Long didnt work");
                        temp = "There was an error.\n Sorry!";
                    }
                    break;
                case 1:
                    try {
                        if (JSONParser.getBoolean("outbound")) {
                            temp += "To - ";
                        }
                        else {
                            temp += "From - ";
                        }

                        temp += JSONParser.getString("contact");

                        temp+="\nCall Start - " + JSONParser.getString("start") +
                              "\nCall End - " + JSONParser.getString("end");
                    }
                    catch (Exception e) {
                        // This ones the most likely fuck up
                        Log.d("Read Log", "Contact/Outbound/Start/End didn't work");
                        temp = "There was an error.\n Sorry!";
                    }
                    break;
                case 2:
                    try {
                        if (JSONParser.getBoolean("outbound")) {
                            temp += "To - ";
                        }
                        else {
                            temp += "From - ";
                        }

                        temp += JSONParser.getString("contact");

                        temp+="\nContent:\n\n" + JSONParser.getString("content");
                    }
                    catch (Exception e) {
                        Log.d("Read Log", "Contact/Outbound/Content didn't work");
                        temp = "There was an error.\n Sorry!";
                    }
                    break;
            }
        }
        catch (Exception e) {
            Log.d("Read Log", "Reading JSON failed - " + e.toString());
            temp = "There was an error.\n Sorry!";
        }

        final AlertDialog dialog = new AlertDialog.Builder(this, R.style.AppThemeDialog).create();
        dialog.setTitle(dh.TYPES[logOut.getInt(0)]);
        dialog.setMessage(temp);
        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Done",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });
        dialog.show();

        logOut.moveToFirst();
        Log.d("Read Log", "Long Click done");

    }

}
