package uk.ac.abertay.forbes.assessment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

import uk.ac.abertay.forbes.assessment.Activity_Record.BoolObj; // Should act like a pointer

public class Service_Record extends IntentService {
    private static final String TAG = "Recording Service";

    public AsyncDatabaseHelper dh;
    public SQLiteDatabase db;

    Activity_Record recordingScreen;

    Integer ttl, gps_wait;
    Boolean call, sms, gps;

    BoolObj power_saving_ptr;

    final Handler ticker = new Handler();

    String gpsProv;
//    SmsReceiver smsReceiver = null;
//    CallReceiver callReceiver = null;
    LocationManager locationManager = null;

    Runnable waitCode = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "1 Min Tick - TTL is " + ttl.toString());

            ttl--;

            if (gps && ContextCompat.checkSelfPermission(recordingScreen.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == 0)
            {
                if ((power_saving_ptr.value ? ttl * 2 : ttl)
                        % gps_wait == 0)
                {
                    Location meme = locationManager.getLastKnownLocation(gpsProv);

                    if (meme == null)
                    {
                        gps = false;
                        Toast.makeText(recordingScreen, "Unable to fetch GPS location", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Log.d(TAG, "LOCATION EXAMPLE - " + meme.getLatitude() + ", " + meme.getLongitude());

                        dh.newActivity(db, 0, "{\n\t\"lat-float\":" + meme.getLatitude()
                                + ",\n\t\"long-float\":" + meme.getLongitude() + "\n}");
                    }


                }
            }

            if (ttl > 0)
                ticker.postDelayed(this, 5000); // TODO FIND 5000 REPLACE WITH 60000
            else {
                recordingScreen.recordingComplete();
            }
        }
    };

    // Copied from documentation
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
        Log.d(TAG, "Handle Intent called");
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
        // stop ticker if its still on the go
        ticker.removeCallbacks(waitCode);

        // Tell the user we stopped.
        Toast.makeText(this, getText(R.string.app_name) + " stopped", Toast.LENGTH_SHORT)
                .show();
    }

    public void setupListeners () {
        // Start listeners
        Log.d(TAG, "Starting Listeners");
        if (gps) {
            Log.d(TAG, "GPS");
            locationManager = (LocationManager) recordingScreen.getSystemService(LOCATION_SERVICE);
            List <String> providers = locationManager.getProviders(true);

            for (String provider: providers) {
                if (!provider.equals("passive")) {
                    gpsProv = provider;
                    break;
                }
            }

            if (gpsProv != null && ContextCompat.checkSelfPermission(recordingScreen.getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == 0) {
                Log.d(TAG, "LOCATION EXAMPLE - " + locationManager.getLastKnownLocation(gpsProv).getAccuracy() + "m Accuracy");
            } else {
                Log.d(TAG, "No provider for GPS");
                Toast.makeText(recordingScreen, "GPS is unavailable", Toast.LENGTH_SHORT).show();

                gps = false;
                Button btn_power_savings = recordingScreen.findViewById(R.id.btn_power_savings);
                btn_power_savings.setVisibility(View.GONE); // All this was doing was turning off GPS
            }
        }

        if (call || sms)
            Toast.makeText(recordingScreen, "Call and Text Recording is currently unavailable", Toast.LENGTH_LONG).show();
//
//        if (call) {
//            Log.d(TAG, "Starting Calls");
//            callReceiver = new CallReceiver();
//            callReceiver.setListener(new CallReceiver.Listener() {
//                @Override
//                public void onCallReceived(String number, Boolean outbound, Date start) {
//                    if (start != null)
//                    {
//                        dh.newActivity(db, 2,"{\n\t\"contact\":\"" + number + "\",\n\t\"outbound\":" + outbound.toString() + "\n\t\"start\":\"" + start.toString() + "\"\n}");
//                    }
//                    else
//                    {
//                        dh.newActivity(db, 2,"{\n\t\"contact\":\"" + number + "\",\n\t\"outbound\":" + outbound.toString() + "\n\t\"start\":\"none\"\n}");
//                    }
//                }
//            });
//        }
//
//        if (sms) {
//            Log.d(TAG, "Starting Texts");
//            smsReceiver = new SmsReceiver();
//            smsReceiver.setListener(new SmsReceiver.Listener() {
//                @Override
//                public void onTextReceived(String sender, String text) {
//                    Log.d("SmsReceiver", "senderNum: " + sender + "; message: " + text);
//                    dh.newActivity(db, 2,"{\n\t\"contact\":\"" + sender + "\",\n\t\"outbound\":false\n\t\"content\":\"" + text + "\"\n}");
//                }
//            });
//
//            registerReceiver(smsReceiver, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
//        }
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
        Log.d(TAG, "Successful run");
        return START_STICKY;
    }

//    public void gotAText(String sender, String text) {
//        dh.newActivity(db, logid,2,"{\n\t\"contact\":\"" + sender + "\",\n\t\"outbound\":false\n\t\"content\":\"" + text + "\"\n}");
//    }
//
//    public void gotACall(String sender, Boolean outbound,@Nullable Date start) {
//        if (start != null)
//        {
//            dh.newActivity(db, logid,2,"{\n\t\"contact\":\"" + sender + "\",\n\t\"outbound\":" + outbound.toString() + "\n\t\"start\":\"" + start.toString() + "\"\n}");
//        }
//        else
//        {
//            dh.newActivity(db, logid,2,"{\n\t\"contact\":\"" + sender + "\",\n\t\"outbound\":" + outbound.toString() + "\n\t\"start\":\"none\"\n}");
//        }
//    }

    public void stopPls() {
        Log.d("Record Service", "Stopping Service - Since you asked so nicely");
        ticker.removeCallbacks(waitCode);
//        mNM.cancel(R.string.app_name);
//        if (sms) unregisterReceiver(smsReceiver);
        super.stopSelf();
    }
}

