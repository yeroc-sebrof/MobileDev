package uk.ac.abertay.forbes.assessment;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import uk.ac.abertay.forbes.assessment.Activity_Record.BoolObj; // Should act like a pointer

public class Service_Record extends IntentService {
    AsyncDatabaseHelper dh;
    SQLiteDatabase db;

    Context recordingScreen;

    Integer ttl, gps_wait;
    Boolean call, sms, gps;

    BoolObj power_saving_ptr;

    final Handler ticker = new Handler();

    Runnable waitCode = new Runnable() {
        @Override
        public void run() {
            Log.d("Recording Service","1 Min Tick - TTL is " + ttl.toString());

            ttl--;

            if (!power_saving_ptr.value)
                // GPS resume method
                Log.d("Recording Service", "GPS stays");
            else
                // GPS Pause
                Log.d("Recording Serive", "GPS goes");

            if (ttl >= 0)
                ticker.postDelayed(this, 5000); // TODO MAKE THIS ONE MIN

        }
    };

    // Copied from documentation
    private NotificationManager mNM;
    private final IBinder mBinder = new LocalBinder();

    public Service_Record(Context context, BoolObj power_saving) {
        super("record");
        power_saving_ptr = power_saving; // This should work I think
        recordingScreen = context;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Get data from intent
        Log.d("Recording Service", "Handle Intent called");
        ttl = intent.getExtras().getInt("MinToLive");

        gps_wait = intent.getExtras().getInt("gps_time");

        call = intent.getExtras().getBoolean("call");
        sms = intent.getExtras().getBoolean("sms");
        gps = intent.getExtras().getBoolean("gps");
    }

    // Copied from Documentation
    public class LocalBinder extends Binder {
        Service_Record getService() {
            return Service_Record.this;
        }
    }

    // Copied from Documentation
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate () {
        // Start DB conn
        db = this.openOrCreateDatabase(dh.DATABASE_NAME, MODE_PRIVATE, null);

        dh = new AsyncDatabaseHelper(this);

        dh.onCreate(db);

        // https://developer.android.com/reference/android/app/Service.html
        // TODO Look into giving this a timer?
        showNotification();
    }

    // Copied from Documentation
    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
//        mNM.cancel(R.string.app_name);

        // Tell the user we stopped.
        Toast.makeText(this,getText(R.string.app_name) + " stopped", Toast.LENGTH_SHORT)
                .show();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Like OnCreate for others why was this not called??!!
        this.onHandleIntent(intent);

        // Have us hear broadcasts
//        ComponentName iThinkThisIsNeeded = new ComponentName(getApplicationContext(), Service_Record.class);
//        PackageManager pacman = this.getPackageManager();
//        pacman.setComponentEnabledSetting(iThinkThisIsNeeded, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
//
        Log.d("Recording Service", "Starting Listeners");
        // Start listeners
        if (call) {
            Log.d("Recording Service", "Starting Calls");
            // do the thing
            // https://github.com/Joh98/CallDefender/blob/master/app/src/main/java/com/aa/calldefender/Home.java
            // https://stackoverflow.com/questions/15945952/no-such-method-getitelephony-to-disconnect-call

        }

        if (sms) {
            Log.d("Recording Service", "Starting Texts");
            // do the thing

        }

        if (gps) {
            Log.d("Recording Service", "GPS Pinged");
            // do the thing

        }

        ticker.post(waitCode); // TODO make this a post delayed


//        pacman.setComponentEnabledSetting(iThinkThisIsNeeded, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);

        Log.d("Recording Service", "Successful run");

        return START_STICKY;
    }

    // copied from documentation
    private void showNotification() {
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(recordingScreen, R.string.app_name,
                new Intent(this, Activity_Record.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.cpt)  // the status icon
                .setTicker(getText(R.string.recording_active))  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.app_name))  // the label of the entry
                .setContentText(getText(R.string.recording_active))  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        mNM.notify(R.string.app_name, notification);
    }

        class GibText extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        }

    @Override
    public boolean stopService(Intent intent) {
        ticker.removeCallbacks(waitCode);

        super.stopService(intent);

        return true;
    }
}

