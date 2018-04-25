package uk.ac.abertay.forbes.assessment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.Objects;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsReceiver";

    private Listener listener;

    public SmsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
            String smsSender = "";
            String smsBody = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                    smsSender = smsMessage.getDisplayOriginatingAddress();
                    smsBody += smsMessage.getMessageBody();
                }
            } else { // Old way to do this that shouldn't be called given the version Im working for
                Bundle smsBundle = intent.getExtras();
                if (smsBundle != null) {
                    Object[] pdus = (Object[]) smsBundle.get("pdus");
                    if (pdus == null) {
                        // Display some error to the user
                        Log.e(TAG, "SmsBundle had no pdus key");
                        return;
                    }
                    SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < messages.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        smsBody += messages[i].getMessageBody();
                    }
                    smsSender = messages[0].getOriginatingAddress();
                }
            }

            if (listener != null) {
                listener.onTextReceived(smsSender, smsBody);
            }

            SQLiteDatabase db = context.openOrCreateDatabase(AsyncDatabaseHelper.DATABASE_NAME, Context.MODE_PRIVATE, null);
            AsyncDatabaseHelper dh = new AsyncDatabaseHelper(context);
            dh.newActivity(db, 2,"{\n\t\"contact\":\"" + smsSender + "\",\n\t\"outbound\":false\n\t\"content\":\"" + smsBody + "\"\n}");
        }
    }

    void setListener(Listener listener) {
        this.listener = listener;
    }

    interface Listener {
        void onTextReceived(String sender, String text);
    }
}

//public class SmsReceiver extends BroadcastReceiver {
//
//    // Get the object of SmsManager
//    final SmsManager sms = SmsManager.getDefault();
//
//    Service_Record parent;
//
//    public void setParent(Service_Record sr) {
//        parent = sr;
//    }
//
//    public void onReceive(Context context, Intent intent) {
//
//        // Retrieves a map of extended data from the intent.
//        final Bundle bundle = intent.getExtras();
//
//        try {
//
//            if (bundle != null) {
//
//                final Object[] pdusObj = (Object[]) bundle.get("pdus");
//
//                for (Object aPdusObj : Objects.requireNonNull(pdusObj)) {
//                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);
//                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();
//                    String message = currentMessage.getDisplayMessageBody();
//
//                    Log.i("SmsReceiver", "senderNum: " + phoneNumber + "; message: " + message);
//
//                    parent.gotAText(phoneNumber, message);
//
//                } // end for loop
//            } // bundle is null
//
//        } catch (Exception e) {
//            Log.e("SmsReceiver", "Exception smsReceiver" + e);
//
//        }
//    }
//}