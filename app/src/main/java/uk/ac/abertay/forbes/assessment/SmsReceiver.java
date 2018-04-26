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
        String smsSender = "";
        String smsBody = "";

        if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
                for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
                    smsSender = smsMessage.getDisplayOriginatingAddress();
                    smsBody += smsMessage.getMessageBody();
                }
        }

        if (listener != null) {
            listener.onTextReceived(smsSender, smsBody);
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