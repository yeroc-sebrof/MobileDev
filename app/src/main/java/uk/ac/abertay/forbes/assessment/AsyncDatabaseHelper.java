package uk.ac.abertay.forbes.assessment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

public class AsyncDatabaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Handler Database Name
    public static final String DATABASE_NAME = "logs_db";

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
    final String[] TYPES = {"GPS", "Phone Call", "SMS"};

    AsyncDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Handler handler;// = new Handler();

    private Activity_ReadLogs readLogsActivity = null;
    private Activity_ReadingLog readingLogActivity = null;

    // Create Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_HANDLER_TABLE =
                "CREATE TABLE IF NOT EXISTS " + HANDLER_TABLE_NAME
                        + "( "
                        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + LOG + " TINYTEXT, "
                        + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                        + " );";

        db.execSQL(CREATE_HANDLER_TABLE);
        Log.d("Database Helper", "Should have made Logs");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        asyncOnUpgrade asyncTask = new asyncOnUpgrade(db);
        asyncTask.execute();
    }

    void readLog(SQLiteDatabase db, int id, Activity_ReadingLog parent) {
        asyncReadLog asyncTask = new asyncReadLog(db);
        readingLogActivity = parent;
        asyncTask.execute(id);
    }

    void readLogs(SQLiteDatabase db, Activity_ReadLogs parent) {
        // This is now Significantly slower than without async. Will continue to async all tasks tho
        asyncReadLogs asyncTask = new asyncReadLogs(db);
        readLogsActivity = parent;
        asyncTask.execute();
    }

    void resetLogs(SQLiteDatabase db) {
        asyncResetLogs asyncTask = new asyncResetLogs(db);
        asyncTask.execute();
    }

    void debugAddActions(SQLiteDatabase db) {
        debugActivities asyncTask = new debugActivities(db);
        asyncTask.execute();
    }

    void makeNewLog(SQLiteDatabase db, String name, Service_Record parent) {
        asyncNewLog asyncTask = new asyncNewLog(db, name, parent);
        asyncTask.execute();
    }

    void newActivity (SQLiteDatabase db, int id, int type, String content) {
        // If this should only be called in a activity does it need to be made async?
        ContentValues insertValue = new ContentValues(0);
        insertValue.put(COLUMN_TYPE, type);
        insertValue.put(COLUMN_CONTENT, content);
        db.insert(LOG + id, null, insertValue);
    }

    void deleteLog(SQLiteDatabase db, int id) {
        asyncDelLog asyncTask = new asyncDelLog(db);
        asyncTask.execute(id);
    }

    public String [] getTYPES()
    { return TYPES; }

    class asyncOnUpgrade extends AsyncTask<Void, Void, Void> {
        private SQLiteDatabase db;

        asyncOnUpgrade(SQLiteDatabase database)
        {
            Log.d("Database Helper", "Activity_ReadLogs task created");
            db = database;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Make sure there's not a old table to interupt this process
            db.execSQL("DROP TABLE IF EXISTS " + HANDLER_TABLE_NAME + "_OLD;");

            // Rename table
            db.execSQL("ALTER TABLE " + HANDLER_TABLE_NAME +
                    " RENAME TO " + HANDLER_TABLE_NAME + "_OLD;");


            onCreate(db);

            // Transfer Data from old table
            // TODO when relevant

            return null;
        }
    }

    class debugActivities extends AsyncTask<Void, Void, Void> {
        private SQLiteDatabase db;

        debugActivities(SQLiteDatabase database)
        {
            Log.d("Database Helper", "Activity_ReadLogs task created");
            db = database;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Cursor lastLog =  db.query(HANDLER_TABLE_NAME, new String[]{COLUMN_ID},
                    null, null, null, null,
                    COLUMN_ID + " DESC", "1");

            lastLog.moveToNext();

            newActivity(db, lastLog.getInt(0), 1,
                    "{\n\t\"contact\":\"The Captain\",\n\t\"outbound\":false,\n\t\"start\":\"Alpha\",\n\t\"end\":\"Omega\"\n}");

            newActivity(db, lastLog.getInt(0), 2,
                    "{\n\t\"contact\":\"The Captain\",\n\t\"outbound\":false,\n\t\"content\":\"Get a bottle of Morgans on the way back\"\n}");

            newActivity(db, lastLog.getInt(0), 2,
                    "{\n\t\"contact\":\"The Captain\",\n\t\"outbound\":true,\n\t\"content\":\"Of Course!\"\n}");

            newActivity(db, lastLog.getInt(0), 2,
                    "{\n\t\"contact\":\"The Captain\",\n\t\"outbound\":true,\n\t\"content\":\"I'm gonna spam you with a massive message for tesing purposes\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nThat Cool?\"\n}");

            lastLog.close();
            return null;
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
            return db.query(LOG + ints[0],
                    new String[] {COLUMN_TYPE, COLUMN_CONTENT, COLUMN_TIMESTAMP},
                    null, null, null, null,
                    COLUMN_ID + " ASC",
                    null);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            readingLogActivity.startList(cursor);
        }
    }

    class asyncReadLogs extends AsyncTask<Void, Void, Cursor> {
        private SQLiteDatabase db;

        asyncReadLogs(SQLiteDatabase database) {
            Log.d("Database Helper", "Activity_ReadLogs task created");
            db = database;
        }

        @Override
        protected Cursor doInBackground(Void... voids) {

            return db.query(HANDLER_TABLE_NAME,
                    new String[]{ COLUMN_ID, LOG, COLUMN_TIMESTAMP },
                    null, null, null, null,
                    COLUMN_ID + " ASC",
                    null);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            readLogsActivity.startList(cursor);
        }
    }

    class asyncResetLogs extends AsyncTask<Void, Void, Cursor> {
        private SQLiteDatabase db;
        asyncResetLogs(SQLiteDatabase database)
        {
            Log.d("Database Helper", "Activity_ReadLogs task created");
            db = database;
        }

        @Override
        protected Cursor doInBackground(Void... voids) {
            return db.query(HANDLER_TABLE_NAME, new String[]{COLUMN_ID, LOG, COLUMN_TIMESTAMP},
                    null, null, null, null,
                    COLUMN_ID + " ASC",
                    null);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            readLogsActivity.logs = cursor;
            readLogsActivity.logs.moveToFirst();
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
            String del = "DROP TABLE IF EXISTS " + LOG + ints[0] + ";";
            db.execSQL(del);
            Log.d("Database Helper","Dropped Table Log " + ints[0]);

            db.delete(HANDLER_TABLE_NAME,
                    COLUMN_ID + "=" + ints[0],
                    null);
            Log.d("Database Helper","Removed Index ID " + ints[0]);

            return null;
        }
    }

    class asyncNewLog extends AsyncTask<Void, Void, Integer> {
        private SQLiteDatabase db;
        private ContentValues insertValue = new ContentValues(0);
        Service_Record SR_ptr;

        asyncNewLog(SQLiteDatabase database, String name, Service_Record calledBy)
        {
            Log.d("Database Helper", "Make new log created");
            db = database;

            SR_ptr = calledBy;

            // For the sake of readability would prefer none to be null
            if (name.equals("")) { name = null; }
            insertValue.put(LOG, name);
            Log.d("Database Helper", "Value Inserted to Index");
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            Integer logIs;
            Log.d("Database Helper", "Make new log called");
            db.insert(HANDLER_TABLE_NAME,
                    null,
                    insertValue);
            Log.d("Database Helper", "Value Inserted to Index");

            // Query to get the new Log ID to work with
            final Cursor logNo = db.query(HANDLER_TABLE_NAME,
                    new String[]{COLUMN_ID},
                    null, null, null, null,
                    COLUMN_ID + " DESC", "1");
            logNo.moveToFirst();
            logIs = logNo.getInt(0);
            logNo.close();

            // Make a new log table
            String CREATE_LOG = "CREATE TABLE IF NOT EXISTS " + LOG + logIs.toString()
                    + "( "
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_TYPE + " INT NOT NULL,"
                    + COLUMN_CONTENT + " TEXT NOT NULL, "
                    + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + ");";
            Log.d("Database Helper", "Executing: " + CREATE_LOG);

            db.execSQL(CREATE_LOG);

            Log.d("Database Helper", "Done making new log" + logIs);
            return logIs;
        }

        @Override
        protected void onPostExecute(Integer log) {
            if (SR_ptr != null){
                Log.d("Database Helper", "New log Post Exe");
                SR_ptr.logid = log;
                SR_ptr.setupListeners();
            }
        }
    }
}


