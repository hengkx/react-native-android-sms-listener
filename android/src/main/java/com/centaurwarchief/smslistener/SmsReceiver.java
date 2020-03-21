package com.centaurwarchief.smslistener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class SmsReceiver extends BroadcastReceiver {
    private ReactApplicationContext mContext;

    private static final String EVENT = "com.centaurwarchief.smslistener:smsReceived";

    public SmsReceiver() {
        super();
    }

    public SmsReceiver(ReactApplicationContext context) {
        mContext = context;
    }

    private void receiveMessages(SmsMessage[] messages) throws Exception {
        if (mContext == null || messages == null) {
            return;
        }

        if (!mContext.hasActiveCatalystInstance()) {
            return;
        }

        Class ownerClass = Class.forName("android.telephony.SmsMessage");
        Method method = ownerClass.getMethod("getSubId");

        Map<String, String> msg = new HashMap<String, String>(messages.length);
        Map<String, Integer> msgSubId = new HashMap<String, Integer>(messages.length);
        Map<String, Long> msgTime = new HashMap<String, Long>(messages.length);
        for (SmsMessage message : messages) {
            String originatingAddress = message.getOriginatingAddress();

            // Check if index with number exists
            if (!msg.containsKey(originatingAddress)) {
                msg.put(originatingAddress, message.getMessageBody());
                int subId = (int) method.invoke(message);
                msgSubId.put(originatingAddress, subId);
                msgTime.put(originatingAddress, message.getTimestampMillis());
            } else {
                String previousParts = msg.get(originatingAddress);
                String msgString = previousParts + message.getMessageBody();
                msg.put(originatingAddress, msgString);
            }
        }

        for (String sender : msg.keySet()) {
            Log.d(
                    SmsListenerPackage.TAG,
                    String.format("%s: %s", sender, msg.get(sender))
            );

            WritableNativeMap receivedMessage = new WritableNativeMap();

            receivedMessage.putString("originatingAddress", sender);
            receivedMessage.putString("body", msg.get(sender));
            receivedMessage.putDouble("time", msgTime.get(sender));
            receivedMessage.putInt("subId", msgSubId.get(sender));

            mContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(EVENT, receivedMessage);
        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SmsMessage[] messages = null;
        ;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        } else {
            try {
                final Bundle bundle = intent.getExtras();

                if (bundle == null || !bundle.containsKey("pdus")) {
                    return;
                }

                final Object[] pdus = (Object[]) bundle.get("pdus");
                messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    byte[] pdu = (byte[]) pdus[i];
                    messages[i] = SmsMessage.createFromPdu(pdu);
                }
            } catch (Exception e) {
                Log.e(SmsListenerPackage.TAG, e.getMessage());
            }
        }

        try {
            receiveMessages(messages);
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
