package autosms.born2go.com.autosms.services;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import autosms.born2go.com.autosms.AutoSMSSingleton;
import autosms.born2go.com.autosms.R;

/**
 * Created by Hue on 8/26/2016.
 */
public class SMSCheckSent extends BroadcastReceiver {
    private static final String TAG = SMSCheckSent.class.getSimpleName();
    private Context context;
    private String auth_key;

    @Override
    public void onReceive(Context context, final Intent intent) {
        this.context = context;
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
        checkSent(intent);
//            }
//        }).start();

    }

    private void checkSent(Intent intent) {
        String id = null, phoneNumber = "", message = "";
        String messageNoti = "";
        if (intent.getExtras() != null) {
            auth_key = intent.getExtras().getString("auth_key");
            id = intent.getExtras().getString("id");
            phoneNumber = intent.getExtras().getString("phoneNumber");
            message = intent.getExtras().getString("message");
            messageNoti = "Sent to " + phoneNumber + " - " + message;
        } else {
            Log.d(TAG, "Extras null");
        }
        int notiId = Integer.valueOf(String.valueOf(System.currentTimeMillis() / 1000));

        switch (getResultCode()) {
            case Activity.RESULT_OK:
                if (auth_key != null && id != null) {
                    updateStatus(id);
                    log(id, "SMS sent -" + messageNoti);
                }
                showNoti("SMS sent -" + messageNoti, notiId, 1);
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                String eGenericFailure = "Generic failure  -" + messageNoti;
                if (auth_key != null && id != null) {
                    log(id, eGenericFailure);
                }
                showNoti(eGenericFailure, notiId + SmsManager.RESULT_ERROR_GENERIC_FAILURE, 0);
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                String eNoservice = "No service -" + messageNoti;
                if (auth_key != null && id != null) {
                    log(id, eNoservice);
                }
                showNoti(eNoservice, notiId + SmsManager.RESULT_ERROR_NO_SERVICE, 0);
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                String eNullPDU = "Null PDU -" + messageNoti;
                if (auth_key != null && id != null) {
                    log(id, eNullPDU);
                }
                showNoti(eNullPDU, notiId + SmsManager.RESULT_ERROR_NULL_PDU, 0);
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                String eRadioOff = "Radio off -" + messageNoti;
                if (auth_key != null && id != null) {
                    log(id, eRadioOff);
                }
                showNoti(eRadioOff, notiId + SmsManager.RESULT_ERROR_RADIO_OFF, 0);
                break;
        }
    }

    private void log(final String id, final String log) {
        try {
            String url = "https://222.255.29.25:8443/laoschoolws/api/sms/log";
            StringRequest objectRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        Log.d(TAG, "response" + response);
                    } catch (Exception e) {
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

                @Override
                public byte[] getBody() {
                    return String.valueOf(id + "-" + log).getBytes();
                }
            };
            AutoSMSSingleton.getInstance().getRequestQueue().add(objectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showNoti(String message, int id, int status) {
        Log.d(TAG, message);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context);
        mBuilder.setContentTitle("LaoSchool-Send SMS");
        mBuilder.setContentText("Sent state : " + ((status == 1) ? "OK - " : "Failse - ") + message);
        mBuilder.setSmallIcon(R.drawable.ic_stop_black_24dp);
        mBuilder.setGroup("Sent SMS state");
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(id, mBuilder.build());
    }

    private void updateStatus(String id) {
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
                    } finally {
                        Log.d(TAG, "updateStatus() -end");
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
}
