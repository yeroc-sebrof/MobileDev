package uk.ac.abertay.forbes.assessment;

import android.Manifest;
import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Telephony;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
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

    SmsReceiver smsReceiver = null;
    CallReceiver callReceiver = null;
    LocationManager locationManager = null;
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "Location Update - " + location.getLatitude() + ", " + location.getLongitude());
            dh.newActivity(db, 0, "{\n\t\"lat-float\":" + location.getLatitude()
                    + ",\n\t\"long-float\":" + location.getLongitude() + "\n}");
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.d(TAG, "Status change for GPS: " + s);
        }

        @Override
        public void onProviderEnabled(String s) {
            Log.d(TAG, "GPS Provider Enabled: " + s);
        }

        @Override
        public void onProviderDisabled(String s) {
            Log.d(TAG, "GPS Provider Disabled: " + s);
        }
    };

    Runnable waitCode = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "1 Min Tick - TTL is " + ttl.toString());

            ttl--;

            if (ttl >= 0)
                ticker.postDelayed(this, 60000
                ); // TODO FIND 60000
                // REPLACE WITH 60000
            else {
                recordingScreen.recordingComplete();
                stopPls();
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


        if(gps && ContextCompat.checkSelfPermission(recordingScreen.getApplicationContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == 0)
        {
            locationManager = (LocationManager) recordingScreen.getSystemService(LOCATION_SERVICE);
            List<String> providers = locationManager.getProviders(true);

            for (String provider:
                 providers) {
                if (!provider.equals(LocationManager.PASSIVE_PROVIDER))
                {
                    locationManager.requestLocationUpdates(provider, gps_wait, 15, locationListener);
                    break;
                }
            }

        }

        if (call) {
            Log.d(TAG, "Starting Calls");
            callReceiver = new CallReceiver();
            callReceiver.setListener(new CallReceiver.Listener() {
                @Override
                public void onCallReceived(String contact, Boolean outbound, String start) {


                    Log.d("Call Received", "senderNum: " + contact + " ended. Started at " + start);
//                    String formattingTimestamp = start.toString().substring(start.toString().length()-1)+
//                            start.toString().substring(9)
//                                    +(!start.toString().substring(10).equals(" ") ? start.toString().substring(10)+" ": " ")
//                                    + ;

                    dh.newActivity(db, 1,"{\n\t\"contact\":\"" + contact + "\",\n\t\"outbound\":" + outbound.toString() + ",\n\t\"start\":\"" + start + "\"\n}");
                }
            });
            recordingScreen.registerReceiver(callReceiver, new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED));
            recordingScreen.registerReceiver(callReceiver, new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL));
        }

        if (sms) {
            Log.d(TAG, "Starting Texts");
            smsReceiver = new SmsReceiver();
            smsReceiver.setListener(new SmsReceiver.Listener() {
                @Override
                public void onTextReceived(String contact, String text) {
                    Log.d("SmsReceiver", "senderNum: " + contact + "; message: " + text);

                    if (!contact.equals(""))
                        dh.newActivity(db, 2,"{\n\t\"contact\":\"" + contact + "\",\n\t\"outbound\":false,\n\t\"content\":\"" + text + "\"\n}");
                    else
                        dh.newActivity(db, 2,"{\n\t\"contact\":\"" + contact + "\",\n\t\"outbound\":true,\n\t\"content\":\"" + text + "\"\n}");
                }
            });

            recordingScreen.registerReceiver(smsReceiver, new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION));
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
        ticker.postDelayed(waitCode, 60000
        );

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
        if (gps) locationManager.removeUpdates(locationListener);
        if (call) recordingScreen.unregisterReceiver(callReceiver);
        if (sms) recordingScreen.unregisterReceiver(smsReceiver);

        super.stopSelf();
    }
}

