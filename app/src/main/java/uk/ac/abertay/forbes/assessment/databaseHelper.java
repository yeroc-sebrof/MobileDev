package uk.ac.abertay.forbes.assessment;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class databaseHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Handler Database Name
    private static final String DATABASE_NAME = "logs_db";

    // Handler Table
    // Handler Table Name
    private static final String HANDLER_TABLE_NAME = "logs";

    // Handle Table Columns
    // Tables will be created == HANDLER_COLUMN_LOG + id
    private static final String HANDLER_COLUMN_ID = "id";
    private static final String HANDLER_COLUMN_LOG = "log";
    private static final String HANDLER_COLUMN_TIMESTAMP = "timestamp";

    databaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table SQL query
        String CREATE_HANDLER_TABLE =
                "CREATE TABLE IF NOT EXISTS" + HANDLER_TABLE_NAME + "("
                        + HANDLER_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + HANDLER_COLUMN_LOG + " TEXT,"
                        + HANDLER_COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP"
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
/*
    public Cursor readLog(SQLiteDatabase db, String id) { // TODO
        String tablename = HANDLER_COLUMN_LOG + id;

        return db.query(tablename, TODO, null, null, null, null, TODO, null);
    }
*/
    public Cursor readLogs(SQLiteDatabase db) { // TODO
        String[] Columns = new String[2];
        Columns[0] = HANDLER_COLUMN_ID;
        Columns[1] = HANDLER_COLUMN_LOG;

        return db.query(HANDLER_TABLE_NAME, Columns,
                null, null, null, null,
                HANDLER_COLUMN_ID + " ASC",
                null);
    }

    private Cursor newLogIndex (SQLiteDatabase db, String name) {
        // For the sake of readability would prefer none to be null
        if (name.equals("")) { name = null; }

        // Make new entry in the log table
        String makeIndexEntry = "INSERT INTO " + HANDLER_TABLE_NAME +
                           "(" + HANDLER_COLUMN_LOG + ")" +
                           "VALUES (" + name + ");";

        db.execSQL(makeIndexEntry);

        // For Columns Array is required
        String[] Columns = new String[1];
        Columns[0] = HANDLER_COLUMN_ID;

        // Return a cursor with one item that it points to
        return db.query(HANDLER_TABLE_NAME, Columns,
                null, null, null, null,
                HANDLER_COLUMN_ID + " DESC", "1");
    }

    public void makeNewLog(SQLiteDatabase db, String name) {
        // Make a new log table
        Cursor logNo = this.newLogIndex(db, name);

        String CREATE_LOG = "CREATE TABLE " + HANDLER_COLUMN_LOG + logNo.getString(0) + ";";

        db.execSQL(CREATE_LOG);
    }

    public void deleteLog(SQLiteDatabase db, int id) {
        String del = "DROP TABLE IF EXISTS " + HANDLER_COLUMN_LOG + id + ";";

        db.execSQL(del);
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