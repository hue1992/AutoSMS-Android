package autosms.born2go.com.autosms.services;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import autosms.born2go.com.autosms.AutoSMSSingleton;
import autosms.born2go.com.autosms.model.HttpsTrustManager;


/**
 * Created by Hue on 8/16/2016.
 */
public class SMSServices extends BroadcastReceiver {
    private String auth_key;
    private String TAG = SMSServices.class.getSimpleName();
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        Log.d(TAG, "onReceive()");
        if (intent.getExtras() != null) {
            auth_key = intent.getExtras().getString("auth_key");
        }
        Log.d(TAG, "auth_key:" + auth_key);
        getOpenMesssage();


    }

    private void getOpenMesssage() {
        HttpsTrustManager.allowAllSSL();

        String url = "https://222.255.29.25:8443/laoschoolws/api/messages/open_sms";
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d(TAG, "getOpenSMS() -response:" + response.toString());
                    JSONArray jsonArray = response.getJSONArray("messageObject");
                    if (jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject msgObj = jsonArray.getJSONObject(i);
                            final int id = msgObj.getInt("id");
                            final String content = msgObj.getString("content");
                            final String phone = msgObj.getString("to_phone");
//                            new Thread(new Runnable() {
//                                @Override
//                                public void run() {
                            Log.d(TAG, "getOpenSMS() -send to:" + phone + ",content" + content);
                            sendSMS(id, phone, content);
//                                }
//                            }).start();

                        }
                    } else {
                        Log.e(TAG, "getOpenSMS() -Empty message");
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "getOpenSMS: -Error message:" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("api_key", "SMS");
                params.put("auth_key", auth_key);
                return params;
            }
        };
        AutoSMSSingleton.getInstance().getRequestQueue().add(objectRequest);


    }

    private void updateStatus(int id) {
        try {
            String url = "https://222.255.29.25:8443/laoschoolws/api/messages/sms_done/" + id;
            JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Log.d(TAG, "updateStatus() -message:" + response.getString("developerMessage").toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "updateStatus() -JSONException:" + e.getMessage());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "updateStatus() -erroResponse:" + error.getMessage());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("api_key", "TEST");
                    params.put("auth_key", auth_key);
                    return params;
                }
            };
            AutoSMSSingleton.getInstance().getRequestQueue().add(objectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void sendSMS(int id, String phoneNumber, String message) {
        try {
            String SENT = "SMS_SENT";

            SmsManager smsManager = SmsManager.getDefault();
//            //divede message
            ArrayList<String> texts = smsManager.divideMessage(message);
            int mMessageSentTotalParts = texts.size();
            if (mMessageSentTotalParts > 1) {
                ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
                for (int j = 0; j < mMessageSentTotalParts; j++) {
                    Intent iSendSMS = new Intent(SENT);
                    iSendSMS.putExtra("auth_key", auth_key);
                    iSendSMS.putExtra("id", String.valueOf(id));
                    iSendSMS.putExtra("phoneNumber", phoneNumber);
                    iSendSMS.putExtra("message", texts.get(j));
                    PendingIntent sentPI = PendingIntent.getBroadcast(context, id + j, iSendSMS, 0);
                    sentIntents.add(sentPI);
                    Log.d(TAG, "Send multipar sms to:" + phoneNumber + ",message:" + texts.get(j));
                }
                smsManager.sendMultipartTextMessage(phoneNumber, null, texts, sentIntents, null);
            } else {
                Intent iSendSMS = new Intent(SENT);
                iSendSMS.putExtra("auth_key", auth_key);
                iSendSMS.putExtra("id", String.valueOf(id));
                iSendSMS.putExtra("phoneNumber", phoneNumber);
                iSendSMS.putExtra("message", message);
                PendingIntent sentPI = PendingIntent.getBroadcast(context, id, iSendSMS, 0);
                smsManager.sendTextMessage(phoneNumber, null, message, sentPI, null);
                Log.d(TAG, "Send sms to:" + phoneNumber + ",message:" + message);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "sendSMS() -Error message:" + ex.getMessage());
        }

    }
}
