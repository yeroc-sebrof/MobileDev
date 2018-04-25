package uk.ac.abertay.forbes.assessment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class IncomingSms extends BroadcastReceiver {
    // Copied from
    // https://androidexample.com/Incomming_SMS_Broadcast_Receiver_-_Android_Example/index.php?view=article_discription&aid=62

    Service_Record parent;

    // Get the object of SmsManager
    final SmsManager sms = SmsManager.getDefault();

    public void setParent(Service_Record sr) {
        parent = sr;
    }

    public void onReceive(Context context, Intent intent) {
        Log.d("SMS Record", "On Receive Triggered");

        // Retrieves a map of extended data from the intent.
        final Bundle bundle = intent.getExtras();

        try {
            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = pdusObj.length; i > 0;) {
                    i--;

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);

                    String senderNum = currentMessage.getDisplayOriginatingAddress();

                    String message = currentMessage.getDisplayMessageBody();

                    Log.i("SmsReceiver", "senderNum: "+ senderNum + "; message: " + message);

                    parent.gotAText(senderNum, message);
                }
            }
        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" + e);

        }
    }
}
