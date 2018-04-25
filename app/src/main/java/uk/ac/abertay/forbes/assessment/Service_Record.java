package uk.ac.abertay.forbes.assessment;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Date;

import uk.ac.abertay.forbes.assessment.Activity_Record.BoolObj; // Should act like a pointer

public class Service_Record extends IntentService {
    public AsyncDatabaseHelper dh;
    public SQLiteDatabase db;

    Activity_Record recordingScreen;

    Integer ttl, gps_wait;
    public Integer logid;
    Boolean call, sms, gps;

    BoolObj power_saving_ptr;

    final Handler ticker = new Handler();

    String gpsProv;
    IncomingSms smsListener = null;
    CallReceiver callReceiver = null;

    Runnable waitCode = new Runnable() {
        @Override
        public void run() {
            Log.d("Recording Service", "1 Min Tick - TTL is " + ttl.toString());

            ttl--;

            if (gps) {
                if (power_saving_ptr.value)
                    // GPS resume method
                    Log.d("Recording Service", "GPS goes");
                else
                    // GPS Pause
                    Log.d("Recording Service", "GPS stays");
            }

            if (ttl % gps_wait == 0 && gps)
            {
                // gps ping
            }

            if (ttl >= 0)
                ticker.postDelayed(this, 5000); // TODO FIND 5000 REPLACE WITH 60000
            else {
                recordingScreen.recordingComplete();
            }
        }
    };

    // Copied from documentation
    NotificationManager mNM;
    final IBinder mBinder = new LocalBinder();

    public Service_Record(String name) {
        super(name);
    }

    public void setReq(Activity_Record context, BoolObj power_saving) {
        recordingScreen = context;
        power_saving_ptr = power_saving; // This should work I think
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

    // Copied from Documentation
    @Override
    public void onDestroy() {
        Log.d("Record Service", "Stopping Service - Since you asked so nicely");

        // Cancel the persistent notification.
        mNM.cancel(R.string.app_name);

        // stop ticker if its still on the go
        ticker.removeCallbacks(waitCode);

        // Tell the user we stopped.
        Toast.makeText(this, getText(R.string.app_name) + " stopped", Toast.LENGTH_SHORT)
                .show();
    }

    @SuppressLint("MissingPermission")
    public void setupListeners () {
        // Start listeners
        Log.d("Recording Service", "Starting Listeners");
        if (gps) {
            Log.d("Recording Service", "GPS Pinged");
            LocationManager locationManager = (LocationManager) recordingScreen.getSystemService(LOCATION_SERVICE);
            Criteria forGPS = new Criteria();
            forGPS.setAccuracy(Criteria.ACCURACY_FINE);

            gpsProv = locationManager.getBestProvider(forGPS, true);

            if (gpsProv == null) {
                Log.d("Recording Service", "LOCATION EXAMPLE - " + locationManager.getLastKnownLocation(gpsProv));
            } else {
                Log.d("Recording Service", "No provider for GPS");
                Toast.makeText(recordingScreen, "GPS is unavailable", Toast.LENGTH_SHORT).show();

                gps = false;
                Button btn_power_savings = recordingScreen.findViewById(R.id.btn_power_savings);
                btn_power_savings.setVisibility(View.GONE); // All this was doing was turning off GPS
            }
        }

        if (call) {
            Log.d("Recording Service", "Starting Calls");
            callReceiver = new CallReceiver();
            callReceiver.setReq(this);
        }

        if (sms) {
            Log.d("Recording Service", "Starting Texts");
            smsListener = new IncomingSms();
            smsListener.setParent(this);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        dh = new AsyncDatabaseHelper(this);
        db = recordingScreen.openOrCreateDatabase(AsyncDatabaseHelper.DATABASE_NAME, MODE_PRIVATE, null);

        dh.onCreate(db);
        // Like OnCreate for others why was this not called??!!
        onHandleIntent(intent);
        dh.makeNewLog(db, intent.getExtras().getString("logname"), this);

        // Start Runnable
        ticker.postDelayed(waitCode, 5000);

        // Service Run successfully
        Log.d("Recording Service", "Successful run");
        return START_STICKY;
    }

    // copied from documentation
    public void showNotification() {
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, R.string.app_name,
                new Intent(this, getClass()), 0);

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

    public void gotAText(String sender, String text) {
        dh.newActivity(db, logid,2,"{\n\t\"contact\":\"" + sender + "\",\n\t\"outbound\":false\n\t\"content\":\"" + text + "\"\n}");
    }

    public void gotACall(String sender, Boolean outbound,@Nullable Date start) {
        if (start != null)
        {
            dh.newActivity(db, logid,2,"{\n\t\"contact\":\"" + sender + "\",\n\t\"outbound\":" + outbound.toString() + "\n\t\"start\":\"" + start.toString() + "\"\n}");
        }
        else
        {
            dh.newActivity(db, logid,2,"{\n\t\"contact\":\"" + sender + "\",\n\t\"outbound\":" + outbound.toString() + "\n\t\"start\":\"none\"\n}");
        }
    }

    public void stopPls() {
        Log.d("Record Service", "Stopping Service - Since you asked so nicely");
        ticker.removeCallbacks(waitCode);
//        mNM.cancel(R.string.app_name);
        super.stopSelf();
    }
}

