package uk.ac.abertay.forbes.assessment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.Dictionary;

public class databaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Handler Database Name
    private static final String DATABASE_NAME = "logs_db";

    // Handler Table
    // Handler Table Name
    private static final String HANDLER_TABLE_NAME = "logs";

    // Handle Table Columns
    // Tables will be created == LOG + id
    private static final String COLUMN_ID = "id";
    private static final String LOG = "log";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_CONTENT = "content";

    // So the storage in the db just has to be a int it will be pulled and the type will be TYPES[<db int>]
    private static final String[] TYPES = {"GPS", "Phone Call", "SMS"};

    databaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table SQL query
        String CREATE_HANDLER_TABLE =
                "CREATE TABLE IF NOT EXISTS " + HANDLER_TABLE_NAME + "("
                        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + LOG + " TINYTEXT,"
                        + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                        + ");";

        // create handler table (logs)
        db.execSQL(CREATE_HANDLER_TABLE);
    }

    // Upgrade method to improve usability
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Make sure there's not a old table to interupt this process
        db.execSQL("DROP TABLE IF EXISTS " + HANDLER_TABLE_NAME + "_OLD;");

        // Rename table
        db.execSQL("ALTER TABLE " + HANDLER_TABLE_NAME +
                  " RENAME TO " + HANDLER_TABLE_NAME + "_OLD;");

        // Create tables again
        onCreate(db);

        // Transfer Data from old table
        // TODO when relevant
    }

    public Cursor readLog(SQLiteDatabase db, int id) {
        return db.query(LOG + id, new String[] {COLUMN_TYPE, COLUMN_CONTENT}, null, null, null, null, COLUMN_ID + " ASC", null);
    }

    public Cursor readLogs(SQLiteDatabase db) { // TODO
        return db.query(HANDLER_TABLE_NAME, new String[]{COLUMN_ID, LOG},
                null, null, null, null,
                COLUMN_ID + " ASC",
                null);
    }

    public void makeNewLog(SQLiteDatabase db, String name) {
        Log.d("Database Helper", "Make new log called");

        // For the sake of readability would prefer none to be null
        if (name.equals("")) { name = null; }

        ContentValues insertValue = new ContentValues(0);
        insertValue.put(LOG, name);
        db.insert(HANDLER_TABLE_NAME, null, insertValue);

        Log.d("Database Helper", "Value Inserted to Index");

        // Query to get the new Log ID to work with
        final Cursor logNo = db.query(HANDLER_TABLE_NAME, new String[]{COLUMN_ID},
                null, null, null, null,
                COLUMN_ID + " DESC", "1");
        logNo.moveToFirst();

        // Make a new log table
        String CREATE_LOG = "CREATE TABLE IF NOT EXISTS " + LOG + logNo.getString(0) + "( "
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TYPE + " INT NOT NULL,"
                + COLUMN_CONTENT + " TEXT NOT NULL"
                + ");";

        Log.d("Database Helper", "Executing: " + CREATE_LOG);

        db.execSQL(CREATE_LOG);
        logNo.close();
        Log.d("Database Helper", "Done making new log");
    }

    public void newActivity (SQLiteDatabase db, int id) {
        db.insert(LOG + id, null, null);
    }

    public void deleteLog(SQLiteDatabase db, int id) {
        String del = "DROP TABLE IF EXISTS " + LOG + id + ";";
        db.execSQL(del);
        Log.d("Database Helper", "Dropped Table Log " + id);

        db.delete(HANDLER_TABLE_NAME, COLUMN_ID + "=" + id, null);
        Log.d("Database Helper", "Removed Index ID " + id);
    }

    public String getDatabaseName()
    { return DATABASE_NAME; }
}

/* OLD CODE
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "notes_db";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        // create notes table
        db.execSQL(Logs.CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + Logs.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }
}
 */