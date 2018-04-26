package uk.ac.abertay.forbes.assessment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
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

    private static final String TAG = "Reading Log";

    TextView txt_currentActivity;

    AsyncDatabaseHelper dh;
    SQLiteDatabase db;

    ArrayAdapter<String> arrayAdapter;
    ListView log_view;

    Boolean contactsPerm; // Still gonna check but I wont ask

    Cursor logCurr;

    @Override // Will be used if I start asking for contacts
    public void onRequestPermissionsResult (int reqCode, String perms[], int[] results) {
        if (reqCode == R.string.app_name)
        {
            for (int x = perms.length; x > 0;)
            {
                x--;
                if (results[x] == PackageManager.PERMISSION_DENIED)
                {
                    switch (perms[x])
                    {
                        case Manifest.permission.READ_EXTERNAL_STORAGE:
                            Toast.makeText(getApplicationContext(),"This application cannot run without Storage Permissions", Toast.LENGTH_LONG)
                                    .show();
                            this.finish();
                            break;

                        case Manifest.permission.READ_CONTACTS:
                            contactsPerm = true;
                            break;
                    }
                }
                else
                {
                    switch (perms[x])
                    {
                        case Manifest.permission.READ_CONTACTS:
                            contactsPerm = true;
                            break;
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_log);

        log_view = findViewById(R.id.log_view);
        txt_currentActivity = findViewById(R.id.txt_currentActivity);

        try {
            log = getIntent().getExtras().getInt("log");
            Log.d(TAG, "Reading " + log);

            txt_currentActivity.append(" Log " + log);
        }
        catch (Exception e) {
            Log.e(TAG, "Intent failed to include an Int - " + e.toString());
            this.finish();
        }

        try {
            db = this.openOrCreateDatabase(AsyncDatabaseHelper.DATABASE_NAME, MODE_PRIVATE, null);
            dh = new AsyncDatabaseHelper(this);
        }
        catch (Exception e) {
            Log.e(TAG, "How did you get this far and couldn't open a database - " + e.toString());
            Toast.makeText(getApplicationContext(), "Unexpected error when opening a Database", Toast.LENGTH_LONG)
                    .show();
            this.finish();
        }

        dh.readLog(db, log,this);

        // TODO, Uncomment this if you want to read the contacts
        // if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS) != 0) requestPermissions(new String[] {Manifest.permission.READ_CONTACTS}, R.string.app_name);
        // else contactsPerm = true;

        Log.d(TAG, "Successful Launch");
    }

    public void startList(Cursor cursor) {
        logCurr = cursor;

        logCurr.moveToNext();

        Log.d(TAG, "Log output has " + logCurr.getCount() + " items");

        List<String> logViewValues = new ArrayList<String>();

        for (int x = logCurr.getCount(); x > 0; x--) {
            logViewValues.add(dh.TYPES[logCurr.getInt(0)] + parseJson());
            Log.d(TAG, x-1 + " items remain");
            logCurr.moveToNext();
        }

        logCurr.moveToFirst();

        arrayAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_list_item_1, logViewValues);

        log_view.setAdapter(arrayAdapter);

        log_view.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                takeItemFurther(view, position);
                return true;
            }
        });

        Log.d(TAG, "Successful Database pull");
    }

    String parseJson () {
        JSONObject JSONFunctionParser;

        try {
            JSONFunctionParser = new JSONObject(logCurr.getString(1));
        }
        catch (Exception e){
            Log.d(TAG, "Parser Broke - " + e.toString());
            return " this item has been corrupted";
        }

        try {
            Log.d(TAG, "Parsing JSON " + dh.getTYPES()[logCurr.getInt(0)] );
        }
        catch (Exception e){
            Log.d(TAG, "Parsing JSON " + logCurr.getInt(0));
            Log.e(TAG, "Parse JSON error " + e.toString());
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
                Log.d(TAG, "Parser Broke - " + e.toString());
                return " this item has been corrupted";
            }
        }

        Log.d(TAG, logCurr.getInt(0) + " does not have a relevant return - " + logCurr.getString(1));
        return " this item has been corrupted";
    }


    void takeItemFurther(View view, int item) {
        // This one should be used on long push of item in log
        Log.d(TAG, "Long Clicked item " + item);

        JSONObject JSONParser;

        logCurr.move(item);
        String content = "";

        try {
            Log.i(TAG, "JSON DATA - " + logCurr.getString(1));
            JSONParser = new JSONObject(logCurr.getString(1));

            switch (logCurr.getInt(0)) {
                case 0:
                    try {
                        content = "Lat - (" + JSONParser.getString("lat-float") + ")" +
                                "\nLong - (" + JSONParser.getString("long-float") + ")";
                    }
                    catch (Exception e) {
                        Log.d(TAG, "Lat/Long didnt work");
                        content = "There was an error.\nSorry!  Err - 1";
                    }
                    content += "\n\n" + "Timestamp: " + logCurr.getString(2);
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

//                        if (contactsPerm) {
//                            content += "\n"; // TODO LOOK INTO A EASIER METHOD OF QUERYING EXISTING CONTACTS
//                        }                    // https://stackoverflow.com/questions/3505865/android-check-phone-number-present-in-contact-list-phone-number-retrieve-fr
                        if (JSONParser.getString("start").equals("none"))
                        {
                            content += "\nMissed Call - " + logCurr.getString(2);
                        }
                        else
                        {
                            content += "\nCall Start - " + JSONParser.getString("start");
                            content += "\nCall End - " + logCurr.getString(2);
                        }
                    }
                    catch (Exception e) {
                        // This ones the most likely fuck up
                        Log.d(TAG, "Contact/Outbound/Start/End didn't work");
                        content = "There was an error.\nSorry!  Err - 2";
                        content += "\n\n" + "Timestamp: " + logCurr.getString(2);
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

//                        if (contactsPerm) {
//                            content += "\n"; // TODO LOOK INTO A EASIER METHOD OF QUERYING EXISTING CONTACTS
//                        }                    // https://stackoverflow.com/questions/3505865/android-check-phone-number-present-in-contact-list-phone-number-retrieve-fr

                        content+="\nContent:\n\n" + JSONParser.getString("content");
                    }
                    catch (Exception e) {
                        Log.d(TAG, "Contact/Outbound/Content didn't work");
                        content = "There was an error.\nSorry!  Err - 3";
                    }
                    content += "\n\n" + "Timestamp: " + logCurr.getString(2);
                    break;
            }
        }
        catch (Exception e) {
            Log.d(TAG, "Reading JSON failed - " + e.toString());
            content = "There was an error.\nSorry!  Err - 4";
        }

        final AlertDialog dialog = new AlertDialog.Builder(this, R.style.CptAppThemeDialog).create();
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
        Log.d(TAG, "Long Click done");

    }

}
