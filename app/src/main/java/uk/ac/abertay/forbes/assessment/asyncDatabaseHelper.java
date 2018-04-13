package uk.ac.abertay.forbes.assessment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

public class asyncDatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Handler Database Name
    public static final String DATABASE_NAME = "logs_db";

    // Handler Table
    // Handler Table Name
    public static final String HANDLER_TABLE_NAME = "logs";

    // Handle Table Columns
    // Tables will be created == LOG + id
    public static final String COLUMN_ID = "id";
    public static final String LOG = "log";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_CONTENT = "content";

    // So the storage in the db just has to be a int it will be pulled and the type will be TYPES[<db int>]
    public final String[] TYPES = {"GPS", "Phone Call", "SMS"};

    asyncDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
    asyncOnCreate asyncTask = new asyncOnCreate(db);
    asyncTask.execute();
    }

    // Upgrade method to improve usability
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        asyncOnUpgrade asyncTask = new asyncOnUpgrade(db);
        asyncTask.doInBackground();

        onCreate(db);
    }

    public Cursor readLog(SQLiteDatabase db, int id) {
        asyncReadLog asyncTask = new asyncReadLog(db);
        return asyncTask.doInBackground(id);
    }

    public Cursor readLogs(SQLiteDatabase db) {
        // This is now Significantly slower than without async. Will continue to async all tasks tho
        asyncReadLogs asyncTask = new asyncReadLogs(db);
        return asyncTask.doInBackground();
    }

    public Cursor lastItemInLogIndex (SQLiteDatabase db) {
        readIndexLastLog asyncTask = new readIndexLastLog(db);
        return asyncTask.doInBackground();
    }

    public void makeNewLog(SQLiteDatabase db, String name) {
        asyncNewLog asyncTask = new asyncNewLog(db, name);
        asyncTask.doInBackground();
    }

    public void newActivity (SQLiteDatabase db, int id, int type, String content) {
        // If this should only be called in a activity does it need to be made async?
        ContentValues insertValue = new ContentValues(0);
        insertValue.put(COLUMN_TYPE, type);
        insertValue.put(COLUMN_CONTENT, content);
        db.insert(LOG + id, null, insertValue);
    }

    public void deleteLog(SQLiteDatabase db, int id) {
        asyncDelLog asyncTask = new asyncDelLog(db);
        asyncTask.doInBackground(id);
    }

    public String getDatabaseName()
    { return DATABASE_NAME; }
}

class asyncOnUpgrade extends AsyncTask<Void, Void, Void> {
    private SQLiteDatabase db;
    asyncOnUpgrade(SQLiteDatabase database)
    {
        Log.d("Database Helper", "ReadLogs task created");
        db = database;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        // Make sure there's not a old table to interupt this process
        db.execSQL("DROP TABLE IF EXISTS " + asyncDatabaseHelper.HANDLER_TABLE_NAME + "_OLD;");

        // Rename table
        db.execSQL("ALTER TABLE " + asyncDatabaseHelper.HANDLER_TABLE_NAME +
                " RENAME TO " + asyncDatabaseHelper.HANDLER_TABLE_NAME + "_OLD;");

        // Transfer Data from old table
        // TODO when relevant

        return null;
    }
}

class readIndexLastLog extends AsyncTask<Void, Void, Cursor> {
    private SQLiteDatabase db;
    readIndexLastLog(SQLiteDatabase database)
    {
        Log.d("Database Helper", "ReadLogs task created");
        db = database;
    }

    @Override
    protected Cursor doInBackground(Void... voids) {
        return db.query(asyncDatabaseHelper.HANDLER_TABLE_NAME,
                new String[]{asyncDatabaseHelper.COLUMN_ID},
                null, null, null, null,
                asyncDatabaseHelper.COLUMN_ID + " DESC",
                "1");
    }
}

class asyncReadLog extends AsyncTask<Integer, Void, Cursor> {
    private SQLiteDatabase db;
    asyncReadLog(SQLiteDatabase database)
    {
        Log.d("Database Helper", "ReadLog task created");
        db = database;
    }

    @Override
    protected Cursor doInBackground(Integer... ints) {
        return db.query(asyncDatabaseHelper.LOG + ints[0],
                new String[] {asyncDatabaseHelper.COLUMN_TYPE,
                              asyncDatabaseHelper.COLUMN_CONTENT,
                              asyncDatabaseHelper.COLUMN_TIMESTAMP},
                null, null, null, null,
                asyncDatabaseHelper.COLUMN_ID + " ASC",
                null);
    }
}

class asyncReadLogs extends AsyncTask<Void, Void, Cursor> {
    private SQLiteDatabase db;
    asyncReadLogs(SQLiteDatabase database)
    {
        Log.d("Database Helper", "ReadLogs task created");
        db = database;
    }

    @Override
    protected Cursor doInBackground(Void... voids) {
        return db.query(asyncDatabaseHelper.HANDLER_TABLE_NAME,
                new String[]{asyncDatabaseHelper.COLUMN_ID,
                             asyncDatabaseHelper.LOG,
                             asyncDatabaseHelper.COLUMN_TIMESTAMP},
                null, null, null, null,
                asyncDatabaseHelper.COLUMN_ID + " ASC",
                null);
    }
}

class asyncOnCreate extends AsyncTask<Void, Void, Void> {
    private SQLiteDatabase db;
    asyncOnCreate(SQLiteDatabase database)
    {
        Log.d("Database Helper", "On create task created");
        db = database;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        String CREATE_HANDLER_TABLE =
                "CREATE TABLE IF NOT EXISTS " + asyncDatabaseHelper.HANDLER_TABLE_NAME + "("
                        + asyncDatabaseHelper.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + asyncDatabaseHelper.LOG + " TINYTEXT,"
                        + asyncDatabaseHelper.COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                        + ");";

        db.execSQL(CREATE_HANDLER_TABLE);
        return null;
    }
}

class asyncDelLog extends AsyncTask<Integer, Void, Void> {
    private SQLiteDatabase db;
    asyncDelLog(SQLiteDatabase database)
    {
        Log.d("Database Helper", "Delete Log created");
        db = database;
    }

    @Override
    protected Void doInBackground(Integer... ints) {
        String del = "DROP TABLE IF EXISTS " + asyncDatabaseHelper.LOG + ints[0] + ";";
        db.execSQL(del);
        Log.d("Database Helper","Dropped Table Log " + ints[0]);

        db.delete(asyncDatabaseHelper.HANDLER_TABLE_NAME,
                asyncDatabaseHelper.COLUMN_ID + "=" + ints[0],
                null);
        Log.d("Database Helper","Removed Index ID " + ints[0]);

        return null;
    }
}

class asyncNewLog extends AsyncTask<Void, Void, Void> {
    private SQLiteDatabase db;
    private ContentValues insertValue = new ContentValues(0);

    asyncNewLog(SQLiteDatabase database, String name)
    {
        Log.d("Database Helper", "Make new log created");
        db = database;

        // For the sake of readability would prefer none to be null
        if (name.equals("")) { name = null; }
        insertValue.put(asyncDatabaseHelper.LOG, name);
        Log.d("Database Helper", "Value Inserted to Index");
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.d("Database Helper", "Make new log called");
        db.insert(asyncDatabaseHelper.HANDLER_TABLE_NAME,
                null,
                insertValue);
        Log.d("Database Helper", "Value Inserted to Index");

        // Query to get the new Log ID to work with
        final Cursor logNo = db.query(asyncDatabaseHelper.HANDLER_TABLE_NAME,
                new String[]{asyncDatabaseHelper.COLUMN_ID},
                null, null, null, null,
                asyncDatabaseHelper.COLUMN_ID + " DESC", "1");
        logNo.moveToFirst();

        // Make a new log table
        String CREATE_LOG = "CREATE TABLE IF NOT EXISTS " + asyncDatabaseHelper.LOG + logNo.getString(0)
                + "( "
                + asyncDatabaseHelper.COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + asyncDatabaseHelper.COLUMN_TYPE + " INT NOT NULL,"
                + asyncDatabaseHelper.COLUMN_CONTENT + " TEXT NOT NULL, "
                + asyncDatabaseHelper.COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ");";
        Log.d("Database Helper", "Executing: " + CREATE_LOG);

        db.execSQL(CREATE_LOG);
        logNo.close();
        Log.d("Database Helper", "Done making new log");
        return null;
    }
}
